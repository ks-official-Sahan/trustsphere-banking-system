package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.AccountStatus;
import com.trustsphere.core.util.SecurityUtil;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Account entity representing bank accounts with balance and transaction history.
 * Includes comprehensive validation and business logic.
 */
@NamedQueries({
        @NamedQuery(
                name = "Account.findByAccountNumber",
                query = "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber"
        ),
        @NamedQuery(
                name = "Account.findByUserId",
                query = "SELECT a FROM Account a WHERE a.user.id = :userId"
        ),
        @NamedQuery(
                name = "Account.findActiveByUserId",
                query = "SELECT a FROM Account a WHERE a.user.id = :userId AND a.status = com.trustsphere.core.enums.AccountStatus.ACTIVE"
        ),
        @NamedQuery(
                name = "Account.findByStatus",
                query = "SELECT a FROM Account a WHERE a.status = :status"
        ),
        @NamedQuery(
                name = "Account.findActiveAccounts",
                query = "SELECT a FROM Account a WHERE a.status = com.trustsphere.core.enums.AccountStatus.ACTIVE"
        ),
        @NamedQuery(
                name = "Account.findByBalanceRange",
                query = "SELECT a FROM Account a WHERE a.balance BETWEEN :minBalance AND :maxBalance"
        )
})
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number"),
        @Index(name = "idx_account_user_id", columnList = "user_id"),
        @Index(name = "idx_account_status", columnList = "status"),
        @Index(name = "idx_account_balance", columnList = "balance"),
        @Index(name = "idx_account_created_at", columnList = "created_at")
})
public class Account extends BaseAuditEntity {

    @NotNull
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Account number can only contain uppercase letters and numbers")
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_type", length = 50)
    private String accountType = "SAVINGS";

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate = new BigDecimal("0.025"); // 2.5% default

    @Column(name = "last_interest_calculation")
    private Instant lastInterestCalculation;

    @Column(name = "daily_transaction_limit", precision = 19, scale = 2)
    private BigDecimal dailyTransactionLimit = new BigDecimal("10000.00");

    @Column(name = "daily_transaction_amount", precision = 19, scale = 2)
    private BigDecimal dailyTransactionAmount = BigDecimal.ZERO;

    @Column(name = "last_transaction_reset")
    private Instant lastTransactionReset;

    @Column(name = "minimum_balance", precision = 19, scale = 2)
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    @OneToMany(mappedBy = "sourceAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "targetAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Transaction> incomingTransactions = new HashSet<>();

    public Account() {}

    public Account(String accountNumber, BigDecimal balance, AccountStatus status, User user) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.user = user;
    }

    /**
     * Creates a new account with generated account number.
     * 
     * @param user the account owner
     * @param initialBalance the initial balance
     * @param accountType the type of account
     * @return a new Account instance
     */
    public static Account createAccount(User user, BigDecimal initialBalance, String accountType) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must be non-negative");
        }
        
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, initialBalance, AccountStatus.ACTIVE, user);
        account.setAccountType(accountType);
        account.setLastTransactionReset(Instant.now());
        account.setLastInterestCalculation(Instant.now());
        return account;
    }

    /**
     * Generates a unique account number.
     * 
     * @return a unique account number
     */
    private static String generateAccountNumber() {
        return "ACC" + SecurityUtil.generateSecureNumericString(12);
    }

    /**
     * Checks if the account has sufficient balance for a transaction.
     * 
     * @param amount the amount to check
     * @return true if sufficient balance, false otherwise
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        BigDecimal availableBalance = this.balance.add(this.overdraftLimit);
        return availableBalance.compareTo(amount) >= 0;
    }

    /**
     * Checks if the account can perform a transaction within daily limits.
     * 
     * @param amount the transaction amount
     * @return true if within limits, false otherwise
     */
    public boolean canPerformTransaction(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Reset daily transaction amount if it's a new day
        if (this.lastTransactionReset == null || 
            Instant.now().isAfter(this.lastTransactionReset.plusSeconds(24 * 60 * 60))) {
            this.dailyTransactionAmount = BigDecimal.ZERO;
            this.lastTransactionReset = Instant.now();
        }
        
        return this.dailyTransactionAmount.add(amount).compareTo(this.dailyTransactionLimit) <= 0;
    }

    /**
     * Records a transaction and updates daily limits.
     * 
     * @param amount the transaction amount
     */
    public void recordTransaction(BigDecimal amount) {
        if (amount == null) {
            return;
        }
        
        // Reset daily transaction amount if it's a new day
        if (this.lastTransactionReset == null || 
            Instant.now().isAfter(this.lastTransactionReset.plusSeconds(24 * 60 * 60))) {
            this.dailyTransactionAmount = BigDecimal.ZERO;
            this.lastTransactionReset = Instant.now();
        }
        
        this.dailyTransactionAmount = this.dailyTransactionAmount.add(amount.abs());
    }

    /**
     * Calculates and applies interest to the account.
     * 
     * @return the interest amount applied
     */
    public BigDecimal calculateAndApplyInterest() {
        if (this.lastInterestCalculation == null) {
            this.lastInterestCalculation = Instant.now();
            return BigDecimal.ZERO;
        }
        
        // Calculate days since last interest calculation
        long daysSinceLastCalculation = (Instant.now().getEpochSecond() - 
                                        this.lastInterestCalculation.getEpochSecond()) / (24 * 60 * 60);
        
        if (daysSinceLastCalculation < 1) {
            return BigDecimal.ZERO;
        }
        
        // Calculate daily interest rate
        BigDecimal dailyRate = this.interestRate.divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);
        
        // Calculate interest for the period
        BigDecimal interest = this.balance.multiply(dailyRate)
                                         .multiply(new BigDecimal(daysSinceLastCalculation))
                                         .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // Apply interest
        this.balance = this.balance.add(interest);
        this.lastInterestCalculation = Instant.now();
        
        return interest;
    }

    /**
     * Checks if the account is active and can perform transactions.
     * 
     * @return true if account is active, false otherwise
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }

    /**
     * Checks if the account is frozen.
     * 
     * @return true if account is frozen, false otherwise
     */
    public boolean isFrozen() {
        return AccountStatus.FROZEN.equals(this.status);
    }

    /**
     * Checks if the account is closed.
     * 
     * @return true if account is closed, false otherwise
     */
    public boolean isClosed() {
        return AccountStatus.CLOSED.equals(this.status);
    }

    // Getters and Setters

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Instant getLastInterestCalculation() {
        return lastInterestCalculation;
    }

    public void setLastInterestCalculation(Instant lastInterestCalculation) {
        this.lastInterestCalculation = lastInterestCalculation;
    }

    public BigDecimal getDailyTransactionLimit() {
        return dailyTransactionLimit;
    }

    public void setDailyTransactionLimit(BigDecimal dailyTransactionLimit) {
        this.dailyTransactionLimit = dailyTransactionLimit;
    }

    public BigDecimal getDailyTransactionAmount() {
        return dailyTransactionAmount;
    }

    public void setDailyTransactionAmount(BigDecimal dailyTransactionAmount) {
        this.dailyTransactionAmount = dailyTransactionAmount;
    }

    public Instant getLastTransactionReset() {
        return lastTransactionReset;
    }

    public void setLastTransactionReset(Instant lastTransactionReset) {
        this.lastTransactionReset = lastTransactionReset;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Set<Transaction> getOutgoingTransactions() {
        return outgoingTransactions;
    }

    public void setOutgoingTransactions(Set<Transaction> outgoingTransactions) {
        this.outgoingTransactions = outgoingTransactions != null ? outgoingTransactions : new HashSet<>();
    }

    public Set<Transaction> getIncomingTransactions() {
        return incomingTransactions;
    }

    public void setIncomingTransactions(Set<Transaction> incomingTransactions) {
        this.incomingTransactions = incomingTransactions != null ? incomingTransactions : new HashSet<>();
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + getId() + '\'' +
                ", accountNumber='" + SecurityUtil.maskSensitiveData(accountNumber, "account") + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", accountType='" + accountType + '\'' +
                ", interestRate=" + interestRate +
                ", dailyTransactionLimit=" + dailyTransactionLimit +
                ", dailyTransactionAmount=" + dailyTransactionAmount +
                ", minimumBalance=" + minimumBalance +
                ", overdraftLimit=" + overdraftLimit +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
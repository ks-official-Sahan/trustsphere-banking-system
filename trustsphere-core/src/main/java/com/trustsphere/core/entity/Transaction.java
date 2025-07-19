package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.TransactionStatus;
import com.trustsphere.core.enums.TransactionType;
import com.trustsphere.core.util.SecurityUtil;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction entity representing financial transactions between accounts.
 * Includes comprehensive validation and business logic.
 */
@NamedQueries({
        @NamedQuery(
                name = "Transaction.findBySourceAccountId",
                query = "SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "Transaction.findByTargetAccountId",
                query = "SELECT t FROM Transaction t WHERE t.targetAccount.id = :accountId ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "Transaction.findByUserId",
                query = "SELECT t FROM Transaction t WHERE t.sourceAccount.user.id = :userId OR t.targetAccount.user.id = :userId ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "Transaction.findByStatus",
                query = "SELECT t FROM Transaction t WHERE t.status = :status"
        ),
        @NamedQuery(
                name = "Transaction.findRecent",
                query = "SELECT t FROM Transaction t ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "Transaction.findByType",
                query = "SELECT t FROM Transaction t WHERE t.type = :type ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "Transaction.findByDateRange",
                query = "SELECT t FROM Transaction t WHERE t.timestamp BETWEEN :startDate AND :endDate ORDER BY t.timestamp DESC"
        ),
        @NamedQuery(
                name = "Transaction.findByAmountRange",
                query = "SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.timestamp DESC"
        )
})
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_source_account", columnList = "source_account_id"),
        @Index(name = "idx_transaction_target_account", columnList = "target_account_id"),
        @Index(name = "idx_transaction_timestamp", columnList = "timestamp"),
        @Index(name = "idx_transaction_status", columnList = "status"),
        @Index(name = "idx_transaction_type", columnList = "type"),
        @Index(name = "idx_transaction_reference", columnList = "reference_number"),
        @Index(name = "idx_transaction_amount", columnList = "amount")
})
public class Transaction extends BaseAuditEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    @NotNull
    @DecimalMin(value = "0.01", message = "Transaction amount must be at least 0.01")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Reference number can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "reference_number", length = 100, unique = true)
    private String referenceNumber;

    @Column(name = "fee_amount", precision = 19, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    public Transaction() {}

    public Transaction(Account sourceAccount, Account targetAccount, BigDecimal amount,
                       TransactionType type, TransactionStatus status) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.timestamp = Instant.now();
        this.referenceNumber = generateReferenceNumber();
    }

    /**
     * Creates a new transaction with generated reference number.
     * 
     * @param sourceAccount the source account
     * @param targetAccount the target account (can be null for deposits/withdrawals)
     * @param amount the transaction amount
     * @param type the transaction type
     * @param description the transaction description
     * @return a new Transaction instance
     */
    public static Transaction createTransaction(Account sourceAccount, Account targetAccount, 
                                              BigDecimal amount, TransactionType type, String description) {
        if (sourceAccount == null) {
            throw new IllegalArgumentException("Source account cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        
        // Validate transaction type requirements
        if (type == TransactionType.TRANSFER && targetAccount == null) {
            throw new IllegalArgumentException("Target account is required for transfer transactions");
        }
        
        Transaction transaction = new Transaction(sourceAccount, targetAccount, amount, type, TransactionStatus.PENDING);
        transaction.setDescription(description);
        return transaction;
    }

    /**
     * Generates a unique reference number for the transaction.
     * 
     * @return a unique reference number
     */
    private String generateReferenceNumber() {
        return "TXN" + Instant.now().getEpochSecond() + SecurityUtil.generateSecureNumericString(6);
    }

    /**
     * Processes the transaction and updates account balances.
     * 
     * @return true if successful, false otherwise
     */
    public boolean process() {
        if (this.status != TransactionStatus.PENDING) {
            return false;
        }
        
        try {
            // Validate accounts are active
            if (!this.sourceAccount.isActive()) {
                this.failureReason = "Source account is not active";
                this.status = TransactionStatus.FAILED;
                return false;
            }
            
            if (this.targetAccount != null && !this.targetAccount.isActive()) {
                this.failureReason = "Target account is not active";
                this.status = TransactionStatus.FAILED;
                return false;
            }
            
            // Check sufficient balance
            if (!this.sourceAccount.hasSufficientBalance(this.amount)) {
                this.failureReason = "Insufficient balance";
                this.status = TransactionStatus.FAILED;
                return false;
            }
            
            // Check daily transaction limits
            if (!this.sourceAccount.canPerformTransaction(this.amount)) {
                this.failureReason = "Daily transaction limit exceeded";
                this.status = TransactionStatus.FAILED;
                return false;
            }
            
            // Update account balances
            this.sourceAccount.setBalance(this.sourceAccount.getBalance().subtract(this.amount));
            this.sourceAccount.recordTransaction(this.amount);
            
            if (this.targetAccount != null) {
                this.targetAccount.setBalance(this.targetAccount.getBalance().add(this.amount));
                this.targetAccount.recordTransaction(this.amount);
            }
            
            // Update transaction status
            this.status = TransactionStatus.COMPLETED;
            this.processedAt = Instant.now();
            
            return true;
            
        } catch (Exception e) {
            this.failureReason = "Processing error: " + e.getMessage();
            this.status = TransactionStatus.FAILED;
            return false;
        }
    }

    /**
     * Cancels the transaction if it's still pending.
     * 
     * @param reason the cancellation reason
     * @return true if cancelled, false otherwise
     */
    public boolean cancel(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            return false;
        }
        
        this.status = TransactionStatus.CANCELLED;
        this.failureReason = reason;
        this.processedAt = Instant.now();
        return true;
    }

    /**
     * Checks if the transaction is completed.
     * 
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }

    /**
     * Checks if the transaction is pending.
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return TransactionStatus.PENDING.equals(this.status);
    }

    /**
     * Checks if the transaction is failed.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }

    /**
     * Checks if the transaction is cancelled.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return TransactionStatus.CANCELLED.equals(this.status);
    }

    /**
     * Gets the total amount including fees.
     * 
     * @return the total amount
     */
    public BigDecimal getTotalAmount() {
        return this.amount.add(this.feeAmount);
    }

    // Getters and Setters

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(Account targetAccount) {
        this.targetAccount = targetAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = SecurityUtil.sanitizeInput(description);
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount != null ? feeAmount : BigDecimal.ZERO;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate != null ? exchangeRate : BigDecimal.ONE;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = SecurityUtil.sanitizeInput(failureReason);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = SecurityUtil.sanitizeInput(userAgent);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + getId() + '\'' +
                ", sourceAccountId='" + (sourceAccount != null ? sourceAccount.getId() : "null") + '\'' +
                ", targetAccountId='" + (targetAccount != null ? targetAccount.getId() : "null") + '\'' +
                ", amount=" + amount +
                ", type=" + type +
                ", status=" + status +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", description='" + description + '\'' +
                ", feeAmount=" + feeAmount +
                ", currency='" + currency + '\'' +
                ", timestamp=" + timestamp +
                ", processedAt=" + processedAt +
                ", failureReason='" + failureReason + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
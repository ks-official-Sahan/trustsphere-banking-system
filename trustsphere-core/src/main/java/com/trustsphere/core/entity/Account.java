package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.AccountStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

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
        )
})
@Entity
@Table(name = "accounts")
public class Account extends BaseAuditEntity implements Serializable {

    @NotNull
    @Size(min = 10, max = 20)
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sourceAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Transaction> outgoingTransactions;

    @OneToMany(mappedBy = "targetAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Transaction> incomingTransactions;

    public Account() {}

    public Account(String accountNumber, BigDecimal balance, AccountStatus status, User user) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.user = user;
    }

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
        this.balance = balance;
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

    public Set<Transaction> getOutgoingTransactions() {
        return outgoingTransactions;
    }

    public void setOutgoingTransactions(Set<Transaction> outgoingTransactions) {
        this.outgoingTransactions = outgoingTransactions;
    }

    public Set<Transaction> getIncomingTransactions() {
        return incomingTransactions;
    }

    public void setIncomingTransactions(Set<Transaction> incomingTransactions) {
        this.incomingTransactions = incomingTransactions;
    }
}
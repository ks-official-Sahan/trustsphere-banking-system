package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.TransactionStatus;
import com.trustsphere.core.enums.TransactionType;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

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
        )
})
@Entity
@Table(name = "transactions")
public class Transaction extends BaseAuditEntity implements Serializable {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    @NotNull
    @DecimalMin(value = "0.01")
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
    private TransactionStatus status;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @Size(max = 100)
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    public Transaction() {}

    public Transaction(Account sourceAccount, Account targetAccount, BigDecimal amount,
                       TransactionType type, TransactionStatus status) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.timestamp = Instant.now();
    }

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
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}
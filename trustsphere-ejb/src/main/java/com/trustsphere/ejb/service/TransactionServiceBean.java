package com.trustsphere.ejb.service;

import com.trustsphere.core.entity.Account;
import com.trustsphere.core.entity.Transaction;
import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.TransactionType;
import com.trustsphere.core.enums.TransactionStatus;
import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.core.util.SecurityUtil;
import com.trustsphere.ejb.api.TransactionServiceRemote;
import com.trustsphere.ejb.dao.TransactionDAO;
import com.trustsphere.ejb.dao.AccountDAO;
import com.trustsphere.ejb.dao.AuditLogDAO;
import com.trustsphere.core.dto.TransactionDTO;
import com.trustsphere.ejb.exception.AccountNotFoundException;
import com.trustsphere.ejb.exception.InsufficientFundsException;
import com.trustsphere.ejb.exception.ValidationException;
import com.trustsphere.ejb.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.UserTransaction;

/**
 * Transaction service bean providing secure and validated transaction processing.
 * Includes comprehensive audit logging and business rule enforcement.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_TELLER"})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TransactionServiceBean implements TransactionServiceRemote {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceBean.class);
    
    // Business constants
    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("50000.00");
    private static final int MAX_DAILY_TRANSACTIONS = 100;

    @Resource
    private UserTransaction utx;

    @EJB
    private AuditLogDAO auditLogDAO;

    @EJB
    private AccountDAO accountDAO;

    @EJB
    private TransactionDAO transactionDAO;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public TransactionDTO transfer(String srcId, String tgtId, BigDecimal amount) {
        logger.info("Processing transfer: {} from {} to {}", amount, srcId, tgtId);
        
        // Validate input parameters
        validateTransferParameters(srcId, tgtId, amount);
        
        try {
            utx.begin();
            utx.setTransactionTimeout(30);

            // Load accounts with pessimistic locking
            Account sourceAccount = accountDAO.findById(srcId);
            Account targetAccount = accountDAO.findById(tgtId);

            if (sourceAccount == null) {
                throw new AccountNotFoundException("Source account not found: " + srcId);
            }
            if (targetAccount == null) {
                throw new AccountNotFoundException("Target account not found: " + tgtId);
            }

            // Validate accounts
            validateAccounts(sourceAccount, targetAccount);

            // Validate transfer amount
            validateTransferAmount(sourceAccount, amount);

            // Check daily limits
            validateDailyLimits(sourceAccount, amount);

            // Create and process transaction
            Transaction transaction = createTransferTransaction(sourceAccount, targetAccount, amount);
            
            // Process the transaction
            if (!transaction.process()) {
                throw new BusinessException("Transaction processing failed: " + transaction.getFailureReason());
            }

            // Persist transaction
            Transaction created = transactionDAO.create(transaction);

            // Update account balances
            accountDAO.update(sourceAccount);
            accountDAO.update(targetAccount);

            // Create audit log
            createAuditLog(sourceAccount, targetAccount, transaction, "TRANSFER_SUCCESS");

            utx.commit();
            
            logger.info("Transfer completed successfully: {}", created.getReferenceNumber());
            return mapToDTO(created);

        } catch (Exception e) {
            try {
                utx.rollback();
                logger.warn("Transaction rollback: {}", e.getMessage());
            } catch (Exception rollbackEx) {
                logger.error("Rollback failed: {}", rollbackEx.getMessage(), rollbackEx);
            }
            
            // Log the error with appropriate level
            if (e instanceof BusinessException) {
                logger.warn("Business exception during transfer: {}", e.getMessage());
            } else {
                logger.error("Unexpected error during transfer: {}", e.getMessage(), e);
            }
            
            throw new BusinessException("Transfer failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates transfer parameters.
     */
    private void validateTransferParameters(String srcId, String tgtId, BigDecimal amount) {
        Map<String, List<String>> errors = new HashMap<>();
        
        if (srcId == null || srcId.trim().isEmpty()) {
            errors.computeIfAbsent("sourceAccountId", k -> new ArrayList<>())
                  .add("Source account ID is required");
        }
        
        if (tgtId == null || tgtId.trim().isEmpty()) {
            errors.computeIfAbsent("targetAccountId", k -> new ArrayList<>())
                  .add("Target account ID is required");
        }
        
        if (srcId != null && tgtId != null && srcId.equals(tgtId)) {
            errors.computeIfAbsent("accounts", k -> new ArrayList<>())
                  .add("Source and target accounts cannot be the same");
        }
        
        if (amount == null) {
            errors.computeIfAbsent("amount", k -> new ArrayList<>())
                  .add("Transfer amount is required");
        } else {
            if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
                errors.computeIfAbsent("amount", k -> new ArrayList<>())
                      .add("Transfer amount must be at least " + MIN_TRANSFER_AMOUNT);
            }
            if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
                errors.computeIfAbsent("amount", k -> new ArrayList<>())
                      .add("Transfer amount cannot exceed " + MAX_TRANSFER_AMOUNT);
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Transfer validation failed", errors);
        }
    }

    /**
     * Validates account status and permissions.
     */
    private void validateAccounts(Account sourceAccount, Account targetAccount) {
        if (!sourceAccount.isActive()) {
            throw new BusinessException("Source account is not active");
        }
        
        if (!targetAccount.isActive()) {
            throw new BusinessException("Target account is not active");
        }
        
        if (sourceAccount.isFrozen()) {
            throw new BusinessException("Source account is frozen");
        }
        
        if (targetAccount.isFrozen()) {
            throw new BusinessException("Target account is frozen");
        }
    }

    /**
     * Validates transfer amount against account balance and limits.
     */
    private void validateTransferAmount(Account sourceAccount, BigDecimal amount) {
        if (!sourceAccount.hasSufficientBalance(amount)) {
            BigDecimal availableBalance = sourceAccount.getBalance().add(sourceAccount.getOverdraftLimit());
            throw new InsufficientFundsException(
                sourceAccount.getId(), 
                availableBalance, 
                amount
            );
        }
    }

    /**
     * Validates daily transaction limits.
     */
    private void validateDailyLimits(Account sourceAccount, BigDecimal amount) {
        // Check daily transaction amount limit
        if (sourceAccount.getDailyTransactionAmount().add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new BusinessException("Daily transfer limit exceeded");
        }
        
        // Check daily transaction count limit
        List<Transaction> todayTransactions = transactionDAO.findBySourceAccountId(sourceAccount.getId());
        long todayCount = todayTransactions.stream()
            .filter(t -> t.getTimestamp().toEpochMilli() > Instant.now().minusSeconds(24 * 60 * 60).toEpochMilli())
            .count();
            
        if (todayCount >= MAX_DAILY_TRANSACTIONS) {
            throw new BusinessException("Daily transaction count limit exceeded");
        }
    }

    /**
     * Creates a transfer transaction.
     */
    private Transaction createTransferTransaction(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        Transaction transaction = Transaction.createTransaction(
            sourceAccount, 
            targetAccount, 
            amount, 
            TransactionType.TRANSFER, 
            "Transfer from " + sourceAccount.getAccountNumber() + " to " + targetAccount.getAccountNumber()
        );
        
        // Set additional transaction details
        transaction.setIpAddress(getCurrentIpAddress());
        transaction.setUserAgent(getCurrentUserAgent());
        transaction.setSessionId(getCurrentSessionId());
        
        return transaction;
    }

    /**
     * Creates audit log entry.
     */
    private void createAuditLog(Account sourceAccount, Account targetAccount, Transaction transaction, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(sourceAccount.getUser().getId());
        auditLog.setAction(action);
        auditLog.setResourceType("TRANSACTION");
        auditLog.setResourceId(transaction.getId());
        auditLog.setSeverityLevel(SeverityLevel.INFO);
        auditLog.setDetails(String.format("Transfer: %s from %s to %s, Reference: %s", 
            transaction.getAmount(), 
            sourceAccount.getAccountNumber(), 
            targetAccount.getAccountNumber(),
            transaction.getReferenceNumber()));
        auditLog.setTimestamp(Instant.now());
        auditLog.setIpAddress(transaction.getIpAddress());
        auditLog.setUserAgent(transaction.getUserAgent());

        auditLogDAO.create(auditLog);
    }

    /**
     * Maps transaction entity to DTO.
     */
    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setSourceAccountId(transaction.getSourceAccount().getId());
        dto.setTargetAccountId(transaction.getTargetAccount() != null ? transaction.getTargetAccount().getId() : null);
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setTimestamp(transaction.getTimestamp());
        return dto;
    }

    // Helper methods for getting current request context
    private String getCurrentIpAddress() {
        // Implementation would get IP from request context
        return "127.0.0.1";
    }

    private String getCurrentUserAgent() {
        // Implementation would get user agent from request context
        return "TrustSphere-Client/1.0";
    }

    private String getCurrentSessionId() {
        // Implementation would get session ID from request context
        return SecurityUtil.generateSecureRandomString(32);
    }
}
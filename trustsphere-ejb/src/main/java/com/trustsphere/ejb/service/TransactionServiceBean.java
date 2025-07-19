package com.trustsphere.ejb.service;

import com.trustsphere.core.entity.Account;
import com.trustsphere.core.entity.Transaction;
import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.TransactionType;
import com.trustsphere.core.enums.TransactionStatus;
import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.ejb.api.TransactionServiceRemote;
import com.trustsphere.ejb.dao.TransactionDAO;
import com.trustsphere.ejb.dao.AccountDAO;
import com.trustsphere.ejb.dao.AuditLogDAO;
import com.trustsphere.core.dto.TransactionDTO;
import com.trustsphere.ejb.exception.AccountNotFoundException;
import com.trustsphere.ejb.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.UserTransaction;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_TELLER"})
public class TransactionServiceBean implements TransactionServiceRemote {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceBean.class);

    @Resource
    private UserTransaction utx;

    @EJB
    private AuditLogDAO auditLogDAO;

    @EJB
    private AccountDAO accountDAO;

    @EJB
    private TransactionDAO transactionDAO;


    @Override
    public TransactionDTO transfer(String srcId, String tgtId, BigDecimal amount) {
        try {
            utx.begin();
            utx.setTransactionTimeout(30);

            Account sourceAccount = accountDAO.findById(srcId);
            Account targetAccount = accountDAO.findById(tgtId);

            if (sourceAccount == null || targetAccount == null) {
                throw new AccountNotFoundException(srcId + " or " + tgtId);
            }

            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(srcId);
            }

            // Debit source account
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
            accountDAO.update(sourceAccount);

            // Credit target account
            targetAccount.setBalance(targetAccount.getBalance().add(amount));
            accountDAO.update(targetAccount);

            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setSourceAccount(sourceAccount);
            transaction.setTargetAccount(targetAccount);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.TRANSFER);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setTimestamp(Instant.now());
            transaction.setReferenceNumber(UUID.randomUUID().toString());

            Transaction created = transactionDAO.create(transaction);

            // Create audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setActorUserId(sourceAccount.getUser().getId());
            auditLog.setAction("TRANSFER");
            auditLog.setResourceType("TRANSACTION");
            auditLog.setResourceId(created.getId());
            auditLog.setSeverityLevel(SeverityLevel.INFO);
            auditLog.setDetails("Transfer: " + amount + " from " + srcId + " to " + tgtId);
            auditLog.setTimestamp(Instant.now());

            auditLogDAO.create(auditLog);

            utx.commit();

            return mapToDTO(created);

        } catch (Exception e) {
            try {
                utx.rollback();
                logger.info("transaction rollback: " +  e.getMessage());
            } catch (Exception rollbackEx) {
                // Log rollback failure
                logger.error(rollbackEx.getMessage());
            }
            logger.error("Transfer failed :{}", e.getMessage(), e);
            throw new RuntimeException("Transfer failed", e);
        }
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setSourceAccountId(transaction.getSourceAccount().getId());
        dto.setTargetAccountId(transaction.getTargetAccount().getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setTimestamp(transaction.getTimestamp());
        return dto;
    }
}
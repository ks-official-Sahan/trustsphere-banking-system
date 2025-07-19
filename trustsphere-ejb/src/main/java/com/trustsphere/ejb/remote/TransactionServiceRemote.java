package com.trustsphere.ejb.remote;

import com.trustsphere.core.dto.TransactionDTO;
import jakarta.ejb.Remote;

import java.math.BigDecimal;
import java.util.List;

@Remote
public interface TransactionServiceRemote {

    TransactionDTO transfer(String srcId, String tgtId, BigDecimal amount);

    List<TransactionDTO> getTransactionsByUser(String userId);
    List<TransactionDTO> getTransactionsByUser(String userId, int offset, int limit);

    List<TransactionDTO> getTransactionsBySourceAccount(String accId);
    List<TransactionDTO> getTransactionsBySourceAccount(String accId, int offset, int limit);

    List<TransactionDTO> getTransactionsByTargetAccount(String accId);
    List<TransactionDTO> getTransactionsByTargetAccount(String accId, int offset, int limit);

}
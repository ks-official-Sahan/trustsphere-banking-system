package com.trustsphere.ejb.api;

import com.trustsphere.ejb.dto.TransactionDTO;
import jakarta.ejb.Remote;

import java.math.BigDecimal;

@Remote
public interface TransactionServiceRemote {

    TransactionDTO transfer(String srcId, String tgtId, BigDecimal amount);
}
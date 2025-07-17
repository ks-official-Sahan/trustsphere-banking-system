package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String acctId) {
        super("Insufficient funds: " + acctId);
    }
}
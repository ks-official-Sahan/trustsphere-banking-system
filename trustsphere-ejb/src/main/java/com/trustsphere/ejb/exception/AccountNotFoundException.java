package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String acctId) {
        super("Account not found: " + acctId);
    }
}

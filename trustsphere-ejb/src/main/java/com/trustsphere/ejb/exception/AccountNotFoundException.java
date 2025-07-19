package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId, "ACCOUNT_NOT_FOUND", true);
    }

    public AccountNotFoundException(String accountId, Throwable cause) {
        super("Account not found: " + accountId, "ACCOUNT_NOT_FOUND", true, cause);
    }
}

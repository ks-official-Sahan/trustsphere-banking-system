package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(String errorCode, String message) {
        super(message, errorCode, true);
    }

    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(message, errorCode, true, cause);
    }
}
package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

/**
 * Base class for all business-related exceptions
 */
@ApplicationException(rollback = true)
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final boolean clientError;

    public BusinessException(String message, String errorCode, boolean clientError) {
        super(message);
        this.errorCode = errorCode;
        this.clientError = clientError;
    }

    public BusinessException(String message, String errorCode, boolean clientError, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.clientError = clientError;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.clientError = true;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.clientError = true;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isClientError() {
        return clientError;
    }
}

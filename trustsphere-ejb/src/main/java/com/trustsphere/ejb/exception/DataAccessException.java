package com.trustsphere.ejb.exception;

//Thrown when database or data access operations fail
public class DataAccessException extends BusinessException {

    public DataAccessException(String message) {
        super(message, "DATA_ACCESS_ERROR", false);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, "DATA_ACCESS_ERROR", false, cause);
    }
}
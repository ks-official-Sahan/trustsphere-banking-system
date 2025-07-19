package com.trustsphere.ejb.exception;

import java.util.List;
import java.util.Map;

/**
 * Thrown when input validation fails
 */
public class ValidationException extends BusinessException {

    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", true);
        this.fieldErrors = null;
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message, "VALIDATION_ERROR", true);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}


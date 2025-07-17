package com.trustsphere.ejb.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }
}
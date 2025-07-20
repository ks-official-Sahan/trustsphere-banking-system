package com.trustsphere.core.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * Secure password hashing utility using BCrypt
 * Provides methods for hashing passwords and verifying them
 */
public class PasswordHasher {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHasher.class.getName());
    private static final int BCRYPT_COST = 12; // Higher cost for banking security
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
        } catch (Exception e) {
            LOGGER.error("Failed to hash password: {}", e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            LOGGER.warn("Password verification failed: {}", e.getMessage());
            return false;
        }
    }
    
    public static String generateSecurePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    

} 
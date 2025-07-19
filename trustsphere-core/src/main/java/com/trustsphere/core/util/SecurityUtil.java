package com.trustsphere.core.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Security utility class for password hashing and validation.
 * Uses bcrypt for secure password hashing with configurable cost factor.
 */
public final class SecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);
    
    // BCrypt cost factor (10 = 2^10 rounds = 1024 iterations)
    private static final int BCRYPT_COST = 12;
    
    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Secure random for additional entropy
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    private SecurityUtil() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Hashes a password using bcrypt with the configured cost factor.
     * 
     * @param plainPassword the plain text password to hash
     * @return the hashed password string
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            String hashedPassword = BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
            logger.debug("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            logger.error("Failed to hash password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Verifies a plain text password against a hashed password.
     * 
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the hashed password to verify against
     * @return true if the password matches, false otherwise
     * @throws IllegalArgumentException if either parameter is null or empty
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Plain password cannot be null or empty");
        }
        
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
            boolean isValid = result.verified;
            
            if (isValid) {
                logger.debug("Password verification successful");
            } else {
                logger.warn("Password verification failed");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Password verification error", e);
            return false;
        }
    }
    
    /**
     * Validates password strength according to security requirements.
     * 
     * @param password the password to validate
     * @return true if password meets requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Validates email format.
     * 
     * @param email the email to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    /**
     * Generates a secure random string for tokens, salts, etc.
     * 
     * @param length the length of the string to generate
     * @return a secure random string
     */
    public static String generateSecureRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Generates a secure random numeric string.
     * 
     * @param length the length of the numeric string to generate
     * @return a secure random numeric string
     */
    public static String generateSecureNumericString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        
        return sb.toString();
    }
    
    /**
     * Sanitizes user input to prevent injection attacks.
     * 
     * @param input the input to sanitize
     * @return the sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    /**
     * Masks sensitive data for logging purposes.
     * 
     * @param data the data to mask
     * @param type the type of data (email, account, etc.)
     * @return the masked data
     */
    public static String maskSensitiveData(String data, String type) {
        if (data == null || data.trim().isEmpty()) {
            return data;
        }
        
        String trimmed = data.trim();
        
        switch (type.toLowerCase()) {
            case "email":
                return maskEmail(trimmed);
            case "account":
                return maskAccountNumber(trimmed);
            case "phone":
                return maskPhoneNumber(trimmed);
            case "ssn":
                return maskSSN(trimmed);
            default:
                return maskGeneric(trimmed);
        }
    }
    
    private static String maskEmail(String email) {
        if (email.length() <= 2) {
            return "***";
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email.charAt(0) + "***@" + (atIndex > 0 ? email.substring(atIndex + 1) : "");
        }
        
        return email.charAt(0) + "***" + email.charAt(atIndex - 1) + "@" + email.substring(atIndex + 1);
    }
    
    private static String maskAccountNumber(String accountNumber) {
        if (accountNumber.length() <= 4) {
            return "****";
        }
        
        return accountNumber.substring(0, 2) + "****" + accountNumber.substring(accountNumber.length() - 2);
    }
    
    private static String maskPhoneNumber(String phone) {
        if (phone.length() <= 4) {
            return "****";
        }
        
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }
    
    private static String maskSSN(String ssn) {
        if (ssn.length() <= 4) {
            return "****";
        }
        
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
    
    private static String maskGeneric(String data) {
        if (data.length() <= 2) {
            return "***";
        }
        
        return data.charAt(0) + "***" + data.charAt(data.length() - 1);
    }
}
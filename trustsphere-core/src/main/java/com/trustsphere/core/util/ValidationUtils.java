package com.trustsphere.core.util;

import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Logger LOGGER = Logger.getLogger(ValidationUtils.class.getName());
    
    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9]{10,20}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s'-]{2,100}$"
    );
    
    private static final Pattern REFERENCE_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9]{8,20}$"
    );
    
    // Business constants
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("999999999.99");
    private static final BigDecimal MIN_ACCOUNT_BALANCE = new BigDecimal("0.00");
    private static final BigDecimal MAX_ACCOUNT_BALANCE = new BigDecimal("999999999999.99");
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches();
    }
    
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }
    
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    public static boolean isValidReferenceNumber(String referenceNumber) {
        if (referenceNumber == null || referenceNumber.trim().isEmpty()) {
            return false;
        }
        return REFERENCE_NUMBER_PATTERN.matcher(referenceNumber.trim()).matches();
    }
    
    public static boolean isValidTransactionAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        
        return amount.compareTo(MIN_TRANSACTION_AMOUNT) >= 0 && 
               amount.compareTo(MAX_TRANSACTION_AMOUNT) <= 0;
    }
    
    public static boolean isValidAccountBalance(BigDecimal balance) {
        if (balance == null) {
            return false;
        }
        
        return balance.compareTo(MIN_ACCOUNT_BALANCE) >= 0 && 
               balance.compareTo(MAX_ACCOUNT_BALANCE) <= 0;
    }
    
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecialChar = true;
        }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
    
    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replaceAll("<script[^>]*>.*?</script>", "")
                   .replaceAll("<[^>]*>", "")
                   .replaceAll("javascript:", "")
                   .replaceAll("on\\w+\\s*=", "");
    }
    
    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        
        try {
            java.util.UUID.fromString(uuid.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public static boolean isValidUserId(String userId) {
        return isValidUUID(userId);
    }
    
    public static boolean isValidAccountId(String accountId) {
        return isValidUUID(accountId);
    }
    
    public static boolean isValidTransactionId(String transactionId) {
        return isValidUUID(transactionId);
    }
    
    public static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    public static boolean isNotNull(Object value) {
        return value != null;
    }
    
    public static boolean isWithinBusinessHours(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        int hour = dateTime.getHour();
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        
        // Business hours: Monday-Friday, 9AM - 5PM
        return dayOfWeek >= 1 && dayOfWeek <= 5 && hour >= 9 && hour < 17;
    }
    
    public static boolean isValidTransactionFrequency(java.time.Instant lastTransactionTime,
                                                     java.time.Instant currentTime) {
        if (lastTransactionTime == null || currentTime == null) {
            return true; // First transaction
        }
        
        // Minimum 30 seconds between transactions
        long secondsBetween = java.time.Duration.between(lastTransactionTime, currentTime).getSeconds();
        return secondsBetween >= 30;
    }
    
    public static void logValidationFailure(String field, String value, String reason) {
        LOGGER.warning(String.format("Validation failed for field '%s' with value '%s': %s", 
                                   field, value, reason));
    }
} 
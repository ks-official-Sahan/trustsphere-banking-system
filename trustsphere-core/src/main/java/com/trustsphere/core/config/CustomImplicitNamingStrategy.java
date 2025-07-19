package com.trustsphere.core.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitEntityNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;

/**
 * Custom implicit naming strategy for consistent database naming conventions.
 * Follows snake_case naming for database objects.
 */
public class CustomImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    @Override
    public Identifier determinePrimaryTableName(ImplicitEntityNameSource source) {
        String entityName = source.getEntityNaming().getJpaEntityName();
        if (entityName == null) {
            entityName = source.getEntityNaming().getClassName();
        }
        
        // Convert to snake_case
        String tableName = toSnakeCase(entityName);
        
        // Add common suffixes if not present
        if (!tableName.endsWith("s")) {
            tableName += "s";
        }
        
        return Identifier.toIdentifier(tableName);
    }

    /**
     * Converts a camelCase or PascalCase string to snake_case.
     * 
     * @param input the input string
     * @return the snake_case string
     */
    private String toSnakeCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        // Handle special cases
        if (input.equals("User")) return "user";
        if (input.equals("Account")) return "account";
        if (input.equals("Transaction")) return "transaction";
        if (input.equals("AuditLog")) return "audit_log";
        if (input.equals("Notification")) return "notification";
        if (input.equals("Role")) return "role";
        
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!first) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
            first = false;
        }
        
        return result.toString();
    }
}
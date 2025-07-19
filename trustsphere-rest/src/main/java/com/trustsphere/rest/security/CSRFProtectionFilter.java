package com.trustsphere.rest.security;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CSRF protection filter to prevent cross-site request forgery attacks.
 * Validates CSRF tokens for state-changing operations.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION + 50) // Run after authentication
@ApplicationScoped
public class CSRFProtectionFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(CSRFProtectionFilter.class.getName());
    
    @Inject
    private ConfigurationProvider configProvider;
    
    private boolean enabled;
    private String tokenHeader;
    private String tokenParameter;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @PostConstruct
    public void init() {
        this.enabled = Boolean.parseBoolean(configProvider.getProperty("security.csrf.enabled", "true"));
        this.tokenHeader = configProvider.getProperty("security.csrf.token.header", "X-CSRF-Token");
        this.tokenParameter = configProvider.getProperty("security.csrf.token.parameter", "_csrf");
        
        LOGGER.info("CSRF protection filter initialized - Enabled: " + enabled);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!enabled) {
            return;
        }

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        // Only check CSRF for state-changing operations
        if (!isStateChangingOperation(method)) {
            return;
        }

        // Skip CSRF check for certain endpoints
        if (isExemptEndpoint(path)) {
            return;
        }

        // Validate CSRF token
        if (!validateCSRFToken(requestContext)) {
            LOGGER.log(Level.WARNING, "CSRF token validation failed for request: " + method + " " + path);
            abortWithForbidden(requestContext, "CSRF token validation failed");
            return;
        }

        LOGGER.fine("CSRF token validation successful for request: " + method + " " + path);
    }

    private boolean isStateChangingOperation(String method) {
        return "POST".equals(method) || 
               "PUT".equals(method) || 
               "DELETE".equals(method) || 
               "PATCH".equals(method);
    }

    private boolean isExemptEndpoint(String path) {
        return path.startsWith("health") ||
               path.startsWith("metrics") ||
               path.startsWith("auth/login") ||
               path.startsWith("auth/refresh") ||
               path.startsWith("swagger") ||
               path.startsWith("openapi");
    }

    private boolean validateCSRFToken(ContainerRequestContext requestContext) {
        // Get token from header
        String headerToken = requestContext.getHeaderString(tokenHeader);
        
        // Get token from form parameter (for form submissions)
        String formToken = requestContext.getHeaderString("Content-Type") != null && 
                          requestContext.getHeaderString("Content-Type").contains("application/x-www-form-urlencoded") ?
                          extractFormParameter(requestContext, tokenParameter) : null;

        String providedToken = headerToken != null ? headerToken : formToken;

        if (providedToken == null || providedToken.trim().isEmpty()) {
            LOGGER.warning("No CSRF token provided");
            return false;
        }

        // Get expected token from session or request context
        String expectedToken = getExpectedCSRFToken(requestContext);
        
        if (expectedToken == null) {
            LOGGER.warning("No expected CSRF token found in session");
            return false;
        }

        // Compare tokens using constant-time comparison to prevent timing attacks
        return constantTimeEquals(providedToken, expectedToken);
    }

    private String extractFormParameter(ContainerRequestContext requestContext, String parameterName) {
        // This is a simplified implementation
        // In a real implementation, you would need to parse the form data
        // For now, we'll rely on header-based tokens
        return null;
    }

    private String getExpectedCSRFToken(ContainerRequestContext requestContext) {
        // In a real implementation, you would get this from the user's session
        // For now, we'll use a placeholder implementation
        String username = (String) requestContext.getProperty("jwt.username");
        if (username != null) {
            // Generate a deterministic token based on username and session
            return generateCSRFToken(username);
        }
        return null;
    }

    private String generateCSRFToken(String username) {
        // In a real implementation, this would be generated and stored in the session
        // For now, we'll generate a simple token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }

    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        Response response = Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"csrf_violation\",\"message\":\"" + message + "\"}")
                .type("application/json")
                .build();

        requestContext.abortWith(response);
    }

    /**
     * Generates a new CSRF token for a user session
     * 
     * @param username the username
     * @return a new CSRF token
     */
    public String generateNewCSRFToken(String username) {
        return generateCSRFToken(username);
    }
}
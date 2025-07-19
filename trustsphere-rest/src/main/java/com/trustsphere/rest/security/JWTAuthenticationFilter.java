package com.trustsphere.rest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Key;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JWTAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(JWTAuthenticationFilter.class.getName());
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ROLES_CLAIM = "roles";
    private static final String USERNAME_CLAIM = "sub";
    private static final String ISSUER_CLAIM = "iss";
    private static final String EXPECTED_ISSUER = "trustsphere";

    @Inject
    private JWTConfiguration jwtConfig;

    private Key signingKey;

    @PostConstruct
    public void init() {
        try {
            // Initialize signing key from configuration
            String secretKey = jwtConfig.getSecretKey();
            if (secretKey != null && !secretKey.isEmpty()) {
                this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
            } else {
                // Fallback to keystore if secret key not provided
                this.signingKey = jwtConfig.getPublicKey();
            }
            LOGGER.info("JWT Authentication Filter initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize JWT Authentication Filter", e);
            throw new RuntimeException("JWT configuration error", e);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip authentication for health checks and public endpoints
        String path = requestContext.getUriInfo().getPath();
        if (isPublicEndpoint(path)) {
            return;
        }

        String authHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            abortWithUnauthorized(requestContext, "Empty JWT token");
            return;
        }

        try {
            Claims claims = validateToken(token);
            SecurityContext securityContext = createSecurityContext(claims, requestContext);
            requestContext.setSecurityContext(securityContext);

            // Add user info to request context for audit logging
            requestContext.setProperty("jwt.username", claims.getSubject());
            requestContext.setProperty("jwt.roles", claims.get(ROLES_CLAIM));

            LOGGER.fine("Successfully authenticated user: " + claims.getSubject());

        } catch (ExpiredJwtException e) {
            LOGGER.log(Level.WARNING, "Expired JWT token", e);
            abortWithUnauthorized(requestContext, "Token expired");
        } catch (UnsupportedJwtException e) {
            LOGGER.log(Level.WARNING, "Unsupported JWT token", e);
            abortWithUnauthorized(requestContext, "Unsupported token format");
        } catch (MalformedJwtException e) {
            LOGGER.log(Level.WARNING, "Malformed JWT token", e);
            abortWithUnauthorized(requestContext, "Malformed token");
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Invalid JWT signature", e);
            abortWithUnauthorized(requestContext, "Invalid token signature");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid JWT token argument", e);
            abortWithUnauthorized(requestContext, "Invalid token");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during JWT validation", e);
            abortWithUnauthorized(requestContext, "Authentication failed");
        }
    }

    private Claims validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(EXPECTED_ISSUER)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Additional custom validations
        validateClaims(claims);

        return claims;
    }

    private void validateClaims(Claims claims) {
        // Validate required claims
        if (claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing or empty subject claim");
        }

        // Validate roles claim exists
        Object rolesClaim = claims.get(ROLES_CLAIM);
        if (rolesClaim == null) {
            throw new IllegalArgumentException("Missing roles claim");
        }

        // Additional business logic validations can be added here
        // e.g., check if user is still active, validate specific role requirements, etc.
    }

    private SecurityContext createSecurityContext(Claims claims, ContainerRequestContext requestContext) {
        String username = claims.getSubject();
        List<String> roles = extractRoles(claims);
        boolean isSecure = requestContext.getSecurityContext().isSecure();

        return new JWTSecurityContext(username, roles, isSecure);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get(ROLES_CLAIM);

        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        } else if (rolesClaim instanceof String) {
            // Handle comma-separated roles string
            String rolesString = (String) rolesClaim;
            return Arrays.asList(rolesString.split(","));
        }

        LOGGER.warning("Invalid roles claim format, defaulting to empty roles");
        return Collections.emptyList();
    }

    private boolean isPublicEndpoint(String path) {
        // Define public endpoints that don't require authentication
        return path.startsWith("health") ||
                path.startsWith("metrics") ||
                path.startsWith("auth/login") ||
                path.startsWith("auth/refresh") ||
                path.startsWith("swagger") ||
                path.startsWith("openapi");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        LOGGER.log(Level.INFO, "Authentication failed: " + message);

        Response response = Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer realm=\"TrustSphere\"")
                .entity("{\"error\":\"unauthorized\",\"message\":\"" + message + "\"}")
                .type("application/json")
                .build();

        requestContext.abortWith(response);
    }

    /**
     * Custom SecurityContext implementation for JWT authentication
     */
    private static class JWTSecurityContext implements SecurityContext {

        private final String username;
        private final List<String> roles;
        private final boolean secure;
        private final Principal userPrincipal;

        public JWTSecurityContext(String username, List<String> roles, boolean secure) {
            this.username = username;
            this.roles = roles != null ? roles : Collections.emptyList();
            this.secure = secure;
            this.userPrincipal = new JWTPrincipal(username);
        }

        @Override
        public Principal getUserPrincipal() {
            return userPrincipal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return roles.contains(role);
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return "Bearer";
        }
    }

    /**
     * JWT Principal implementation
     */
    private static class JWTPrincipal implements Principal {

        private final String name;

        public JWTPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JWTPrincipal that = (JWTPrincipal) obj;
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "JWTPrincipal{name='" + name + "'}";
        }
    }
}
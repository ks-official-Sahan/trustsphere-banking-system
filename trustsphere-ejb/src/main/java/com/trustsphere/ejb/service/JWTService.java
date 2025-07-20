package com.trustsphere.ejb.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Stateless
public class JWTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTService.class);
    
    // Default configuration - in a real app, this would be injected from configuration
    private static final String SECRET_KEY = "your-256-bit-secret-key-here-must-be-long-enough-for-hs256-algorithm";
    private static final String ISSUER = "trustsphere";
    private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 604800; // 7 days
    
    private final Key signingKey;

    public JWTService() {
        this.signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generate an access token for authenticated user
     */
    public String generateAccessToken(String userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(ACCESS_TOKEN_EXPIRATION_SECONDS, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .setIssuer(ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("tokenType", "access")
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate a refresh token for authenticated user
     */
    public String generateRefreshToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expiration = now.plus(REFRESH_TOKEN_EXPIRATION_SECONDS, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(email)
                .setIssuer(ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .claim("userId", userId)
                .claim("tokenType", "refresh")
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate and parse JWT token
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.warn("Token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extract user ID from token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", String.class);
    }

    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Check if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = validateToken(token);
        return "refresh".equals(claims.get("tokenType"));
    }

    /**
     * Get token expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return ACCESS_TOKEN_EXPIRATION_SECONDS;
    }

    public long getRefreshTokenExpirationSeconds() {
        return REFRESH_TOKEN_EXPIRATION_SECONDS;
    }
}
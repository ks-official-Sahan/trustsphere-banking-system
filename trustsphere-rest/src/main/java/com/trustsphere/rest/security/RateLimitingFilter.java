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
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rate limiting filter to prevent API abuse.
 * Implements sliding window rate limiting per IP address.
 */
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100) // Run before authentication
@ApplicationScoped
public class RateLimitingFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(RateLimitingFilter.class.getName());
    
    private final ConcurrentHashMap<String, RateLimitWindow> rateLimitMap = new ConcurrentHashMap<>();
    
    @Inject
    private ConfigurationProvider configProvider;
    
    private int requestsPerMinute;
    private int requestsPerHour;
    private boolean enabled;
    
    @PostConstruct
    public void init() {
        this.enabled = Boolean.parseBoolean(configProvider.getProperty("security.rate.limiting.enabled", "true"));
        this.requestsPerMinute = Integer.parseInt(configProvider.getProperty("security.rate.limiting.requests.per.minute", "100"));
        this.requestsPerHour = Integer.parseInt(configProvider.getProperty("security.rate.limiting.requests.per.hour", "1000"));
        
        LOGGER.info("Rate limiting filter initialized - Enabled: " + enabled + 
                   ", Per minute: " + requestsPerMinute + 
                   ", Per hour: " + requestsPerHour);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!enabled) {
            return;
        }

        String clientIp = getClientIpAddress(requestContext);
        String path = requestContext.getUriInfo().getPath();

        // Skip rate limiting for health checks and metrics
        if (isExemptEndpoint(path)) {
            return;
        }

        if (isRateLimited(clientIp)) {
            LOGGER.log(Level.WARNING, "Rate limit exceeded for IP: " + clientIp);
            abortWithTooManyRequests(requestContext, "Rate limit exceeded");
            return;
        }

        // Update rate limit counters
        updateRateLimitCounters(clientIp);
    }

    private String getClientIpAddress(ContainerRequestContext requestContext) {
        // Check for X-Forwarded-For header (for proxy scenarios)
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Fallback to remote address
        return requestContext.getHeaderString("X-Forwarded-For") != null ? 
               requestContext.getHeaderString("X-Forwarded-For") : "unknown";
    }

    private boolean isExemptEndpoint(String path) {
        return path.startsWith("health") ||
               path.startsWith("metrics") ||
               path.startsWith("swagger") ||
               path.startsWith("openapi");
    }

    private boolean isRateLimited(String clientIp) {
        RateLimitWindow window = rateLimitMap.get(clientIp);
        if (window == null) {
            return false;
        }

        Instant now = Instant.now();
        
        // Check minute limit
        if (now.isAfter(window.minuteWindowStart.plusSeconds(60))) {
            window.minuteCount.set(0);
            window.minuteWindowStart = now;
        } else if (window.minuteCount.get() >= requestsPerMinute) {
            return true;
        }

        // Check hour limit
        if (now.isAfter(window.hourWindowStart.plusSeconds(3600))) {
            window.hourCount.set(0);
            window.hourWindowStart = now;
        } else if (window.hourCount.get() >= requestsPerHour) {
            return true;
        }

        return false;
    }

    private void updateRateLimitCounters(String clientIp) {
        RateLimitWindow window = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitWindow());
        Instant now = Instant.now();

        // Update minute counter
        if (now.isAfter(window.minuteWindowStart.plusSeconds(60))) {
            window.minuteCount.set(1);
            window.minuteWindowStart = now;
        } else {
            window.minuteCount.incrementAndGet();
        }

        // Update hour counter
        if (now.isAfter(window.hourWindowStart.plusSeconds(3600))) {
            window.hourCount.set(1);
            window.hourWindowStart = now;
        } else {
            window.hourCount.incrementAndGet();
        }
    }

    private void abortWithTooManyRequests(ContainerRequestContext requestContext, String message) {
        Response response = Response.status(Response.Status.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", requestsPerMinute)
                .header("X-RateLimit-Remaining", "0")
                .header("Retry-After", "60")
                .entity("{\"error\":\"rate_limit_exceeded\",\"message\":\"" + message + "\"}")
                .type("application/json")
                .build();

        requestContext.abortWith(response);
    }

    /**
     * Rate limit window for tracking requests per time period
     */
    private static class RateLimitWindow {
        private AtomicInteger minuteCount = new AtomicInteger(0);
        private AtomicInteger hourCount = new AtomicInteger(0);
        private Instant minuteWindowStart = Instant.now();
        private Instant hourWindowStart = Instant.now();
    }
}
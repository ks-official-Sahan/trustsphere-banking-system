package com.trustsphere.rest.security;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION - 100)
@ApplicationScoped
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);
    
    // Rate limiting configuration
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int WINDOW_SIZE_MINUTES = 1;
    private static final int WINDOW_SIZE_HOURS = 60;
    
    // Store request counts per client IP
    private final ConcurrentHashMap<String, RequestWindow> requestWindows = new ConcurrentHashMap<>();
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String clientIp = getClientIp(requestContext);
        String path = requestContext.getUriInfo().getPath();
        
        // Skip rate limiting for health checks and public endpoints
        if (isExemptFromRateLimit(path)) {
            return;
        }
        
        // Check rate limits
        if (!isWithinRateLimit(clientIp, path)) {
            LOGGER.warn("Rate limit exceeded for client: {} on path: {}", clientIp, path);
            abortWithRateLimitExceeded(requestContext);
            return;
        }
        
        // Update request count
        updateRequestCount(clientIp, path);
    }
    
    private String getClientIp(ContainerRequestContext requestContext) {
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return requestContext.getHeaderString("X-Client-IP");
    }
    
    private boolean isExemptFromRateLimit(String path) {
        return path.startsWith("health") ||
               path.startsWith("metrics") ||
               path.startsWith("swagger") ||
               path.startsWith("openapi") ||
               path.startsWith("auth/login") ||
               path.startsWith("auth/refresh");
    }
    
    private boolean isWithinRateLimit(String clientIp, String path) {
        String key = clientIp + ":" + path;
        RequestWindow window = requestWindows.get(key);
        
        if (window == null) {
            return true; // First request
        }
        
        Instant now = Instant.now();
        
        // Check minute rate limit
        if (window.getMinuteCount() >= MAX_REQUESTS_PER_MINUTE && 
            now.isBefore(window.getMinuteWindowEnd())) {
            return false;
        }
        
        // Check hour rate limit
        if (window.getHourCount() >= MAX_REQUESTS_PER_HOUR && 
            now.isBefore(window.getHourWindowEnd())) {
            return false;
        }
        
        return true;
    }
    
    private void updateRequestCount(String clientIp, String path) {
        String key = clientIp + ":" + path;
        Instant now = Instant.now();
        
        requestWindows.compute(key, (k, existingWindow) -> {
            if (existingWindow == null) {
                return new RequestWindow(now);
            }
            
            // Reset counters if window has expired
            if (now.isAfter(existingWindow.getMinuteWindowEnd())) {
                existingWindow.resetMinuteCount();
                existingWindow.setMinuteWindowEnd(now.plusSeconds(60));
            }
            
            if (now.isAfter(existingWindow.getHourWindowEnd())) {
                existingWindow.resetHourCount();
                existingWindow.setHourWindowEnd(now.plusSeconds(3600));
            }
            
            // Increment counters
            existingWindow.incrementMinuteCount();
            existingWindow.incrementHourCount();
            
            return existingWindow;
        });
    }
    
    private void abortWithRateLimitExceeded(ContainerRequestContext requestContext) {
        Response response = Response.status(Response.Status.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .entity("{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests. Please try again later.\"}")
                .type("application/json")
                .build();
        
        requestContext.abortWith(response);
    }
    
    private static class RequestWindow {
        private final AtomicInteger minuteCount = new AtomicInteger(0);
        private final AtomicInteger hourCount = new AtomicInteger(0);
        private Instant minuteWindowEnd;
        private Instant hourWindowEnd;
        
        public RequestWindow(Instant startTime) {
            this.minuteWindowEnd = startTime.plusSeconds(60);
            this.hourWindowEnd = startTime.plusSeconds(3600);
            this.minuteCount.incrementAndGet();
            this.hourCount.incrementAndGet();
        }
        
        public int getMinuteCount() {
            return minuteCount.get();
        }
        
        public int getHourCount() {
            return hourCount.get();
        }
        
        public void incrementMinuteCount() {
            minuteCount.incrementAndGet();
        }
        
        public void incrementHourCount() {
            hourCount.incrementAndGet();
        }
        
        public void resetMinuteCount() {
            minuteCount.set(0);
        }
        
        public void resetHourCount() {
            hourCount.set(0);
        }
        
        public Instant getMinuteWindowEnd() {
            return minuteWindowEnd;
        }
        
        public Instant getHourWindowEnd() {
            return hourWindowEnd;
        }
        
        public void setMinuteWindowEnd(Instant minuteWindowEnd) {
            this.minuteWindowEnd = minuteWindowEnd;
        }
        
        public void setHourWindowEnd(Instant hourWindowEnd) {
            this.hourWindowEnd = hourWindowEnd;
        }
    }
    
    public void cleanup() {
        Instant now = Instant.now();
        requestWindows.entrySet().removeIf(entry -> {
            RequestWindow window = entry.getValue();
            return now.isAfter(window.getMinuteWindowEnd()) && 
                   now.isAfter(window.getHourWindowEnd());
        });
    }
} 
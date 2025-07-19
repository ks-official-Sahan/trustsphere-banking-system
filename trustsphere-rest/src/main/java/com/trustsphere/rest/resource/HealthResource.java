package com.trustsphere.rest.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Health check resource for monitoring system status.
 * Provides health information for load balancers and monitoring systems.
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class HealthResource {

    private static final Logger LOGGER = Logger.getLogger(HealthResource.class.getName());
    
    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager entityManager;
    
    private final Instant startTime = Instant.now();

    @GET
    public Response getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now());
        health.put("uptime", System.currentTimeMillis() - startTime.toEpochMilli());
        health.put("version", "1.0.0");
        
        // Add system metrics
        health.put("system", getSystemMetrics());
        
        // Add database health
        health.put("database", getDatabaseHealth());
        
        return Response.ok(health).build();
    }

    @GET
    @Path("/liveness")
    public Response getLiveness() {
        // Simple liveness check - just return OK if the application is running
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "UP");
        liveness.put("timestamp", Instant.now());
        
        return Response.ok(liveness).build();
    }

    @GET
    @Path("/readiness")
    public Response getReadiness() {
        Map<String, Object> readiness = new HashMap<>();
        
        try {
            // Check database connectivity
            boolean dbHealthy = checkDatabaseConnectivity();
            
            if (dbHealthy) {
                readiness.put("status", "UP");
                readiness.put("database", "UP");
            } else {
                readiness.put("status", "DOWN");
                readiness.put("database", "DOWN");
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(readiness).build();
            }
            
            readiness.put("timestamp", Instant.now());
            return Response.ok(readiness).build();
            
        } catch (Exception e) {
            LOGGER.warning("Readiness check failed: " + e.getMessage());
            readiness.put("status", "DOWN");
            readiness.put("error", e.getMessage());
            readiness.put("timestamp", Instant.now());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(readiness).build();
        }
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        metrics.put("memory", Map.of(
            "heapUsed", memoryBean.getHeapMemoryUsage().getUsed(),
            "heapMax", memoryBean.getHeapMemoryUsage().getMax(),
            "nonHeapUsed", memoryBean.getNonHeapMemoryUsage().getUsed(),
            "nonHeapMax", memoryBean.getNonHeapMemoryUsage().getMax()
        ));
        
        // Thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        metrics.put("threads", Map.of(
            "active", threadBean.getThreadCount(),
            "peak", threadBean.getPeakThreadCount(),
            "daemon", threadBean.getDaemonThreadCount()
        ));
        
        // Runtime metrics
        Runtime runtime = Runtime.getRuntime();
        metrics.put("runtime", Map.of(
            "availableProcessors", runtime.availableProcessors(),
            "freeMemory", runtime.freeMemory(),
            "totalMemory", runtime.totalMemory(),
            "maxMemory", runtime.maxMemory()
        ));
        
        return metrics;
    }

    private Map<String, Object> getDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            boolean isHealthy = checkDatabaseConnectivity();
            dbHealth.put("status", isHealthy ? "UP" : "DOWN");
            dbHealth.put("timestamp", Instant.now());
            
            if (isHealthy) {
                dbHealth.put("message", "Database connection is healthy");
            } else {
                dbHealth.put("message", "Database connection failed");
            }
            
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("message", "Database health check failed: " + e.getMessage());
            dbHealth.put("timestamp", Instant.now());
        }
        
        return dbHealth;
    }

    private boolean checkDatabaseConnectivity() {
        try {
            // Simple database connectivity check
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            LOGGER.warning("Database connectivity check failed: " + e.getMessage());
            return false;
        }
    }
}
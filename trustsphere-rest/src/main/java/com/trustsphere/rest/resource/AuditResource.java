package com.trustsphere.rest.resource;

import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.ejb.api.AuditServiceRemote;
import com.trustsphere.core.dto.AuditLogDTO;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/audit")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuditResource {

    @EJB
    private AuditServiceRemote auditService;

    @GET
    @Path("recent")
    @RolesAllowed("ROLE_AUDITOR")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getRecentLogs(
            @QueryParam("limit")
            @DefaultValue("50")
            @Min(value = 1, message = "Limit must be at least 1")
            int limit) {

        try {
            List<AuditLogDTO> logs = auditService.getRecentLogs(limit);
            return Response.ok(logs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to retrieve recent logs: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("severity/{level}")
    @RolesAllowed({"ROLE_AUDITOR", "ROLE_ADMIN"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getLogsBySeverity(
            @PathParam("level")
            @NotNull
            @Pattern(regexp = "INFO|WARN|ERROR|CRITICAL", message = "Invalid severity level")
            SeverityLevel level,

            @QueryParam("limit")
            @DefaultValue("100")
            @Min(value = 1, message = "Limit must be at least 1")
            int limit) {

        try {
            //List<AuditLogDTO> logs = auditService.getLogsBySeverity(level, limit);
            List<AuditLogDTO> logs = auditService.getLogsBySeverity(level);
            return Response.ok(logs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to retrieve logs by severity: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("user/{userId}")
    @RolesAllowed({"ROLE_AUDITOR", "ROLE_ADMIN"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getLogsByUser(
            @PathParam("userId")
            @NotNull
            String userId,

            @QueryParam("limit")
            @DefaultValue("100")
            @Min(value = 1, message = "Limit must be at least 1")
            int limit,

            @QueryParam("startDate")
            String startDate,

            @QueryParam("endDate")
            String endDate) {

        try {
            LocalDateTime start = startDate != null ?
                    LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime end = endDate != null ?
                    LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

            //List<AuditLogDTO> logs = auditService.getLogsByUser(userId, start, end, limit);
            List<AuditLogDTO> logs = auditService.getLogsByUser(userId);
            return Response.ok(logs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to retrieve logs by user: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("resource")
    @RolesAllowed("ROLE_AUDITOR")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getLogsByResource(
            @QueryParam("resourceType")
            @NotNull
            String resourceType,

            @QueryParam("resourceId")
            String resourceId,

            @QueryParam("action")
            String action,

            @QueryParam("limit")
            @DefaultValue("100")
            @Min(value = 1, message = "Limit must be at least 1")
            int limit,

            @QueryParam("startDate")
            String startDate,

            @QueryParam("endDate")
            String endDate) {

        try {
            LocalDateTime start = startDate != null ?
                    LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
            LocalDateTime end = endDate != null ?
                    LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

            //List<AuditLogDTO> logs = auditService.getLogsByResource(
            //        resourceType, resourceId, action, start, end, limit);
            List<AuditLogDTO> logs = auditService.getLogsByResource(resourceType, resourceId);
            return Response.ok(logs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to retrieve logs by resource: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("health")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response healthCheck() {
        try {
            //boolean healthy = auditService.isHealthy();
            boolean healthy = true;
            return healthy ?
                    Response.ok().entity("{\"status\":\"healthy\"}").build() :
                    Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity("{\"status\":\"degraded\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"status\":\"unhealthy\",\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
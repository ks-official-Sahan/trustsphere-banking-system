package com.trustsphere.rest.resource;

import com.trustsphere.ejb.api.NotificationServiceRemote;
import com.trustsphere.core.dto.NotificationDTO;
import com.trustsphere.core.enums.NotificationType;

import com.trustsphere.rest.model.ErrorResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class NotificationResource {

    private static final Logger LOGGER = Logger.getLogger(NotificationResource.class.getName());

    // Default pagination limits
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 200;

    @EJB
    private NotificationServiceRemote notifService;

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("user/{userId}")
    @RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getNotificationsByUser(
            @PathParam("userId") String userId,

            @QueryParam("page") @DefaultValue("0") int page,

            @QueryParam("size") @DefaultValue("50") int size,

            @QueryParam("includeRead") @DefaultValue("true") boolean includeRead) {

        try {
            // Input validation
            ValidationResult validationResult = validateUserNotificationRequest(userId, page, size);
            if (!validationResult.isValid()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_FAILED", validationResult.getMessage()))
                        .build();
            }

            String currentUser = securityContext.getUserPrincipal().getName();
            boolean isAdmin = securityContext.isUserInRole("ROLE_ADMIN");

            // Enforce data isolation - users can only see their own notifications unless admin
            if (!isAdmin && !currentUser.equals(userId)) {
                LOGGER.warning("Unauthorized notification access attempt: user=" + currentUser +
                        " requested=" + userId);
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("UNAUTHORIZED_ACCESS", "Access denied"))
                        .build();
            }

            LOGGER.info("Retrieving notifications for user: " + userId +
                    " page: " + page + " size: " + size + " includeRead: " + includeRead);

            //List<NotificationDTO> notifications = notifService.getNotificationsByUser(
            //        userId, page, size, includeRead);
            List<NotificationDTO> notifications = notifService.getNotificationsByUser(userId);

            // Get total count for pagination metadata
            //long totalCount = notifService.getNotificationCountByUser(userId, includeRead);
            long totalCount = notifications.size();

            NotificationPageResponse response = new NotificationPageResponse(
                    notifications, page, size, totalCount);

            LOGGER.info("Retrieved " + notifications.size() + " notifications for user: " + userId);
            return Response
                    .ok(response)
                    .build();

        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Unauthorized notification access: " + userId, e);
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("UNAUTHORIZED_ACCESS", "Access denied"))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid user notification request: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("USER_NOT_FOUND", "User not found"))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve user notifications: " + userId, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }
    }

    @GET
    @Path("type/{type}")
    @RolesAllowed({"ROLE_USER", "ROLE_ADMIN"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getNotificationsByType(
            @PathParam("type") String type,

            @QueryParam("page") @DefaultValue("0") int page,

            @QueryParam("size") @DefaultValue("50") int size,

            @QueryParam("includeRead") @DefaultValue("true") boolean includeRead) {

        try {
            // Input validation
            ValidationResult validationResult = validateTypeNotificationRequest(type, page, size);
            if (!validationResult.isValid()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_FAILED", validationResult.getMessage()))
                        .build();
            }

            String currentUser = securityContext.getUserPrincipal().getName();
            boolean isAdmin = securityContext.isUserInRole("ROLE_ADMIN");

            // Parse and validate notification type
            NotificationType notificationType;
            try {
                notificationType = NotificationType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_TYPE", "Invalid notification type: " + type))
                        .build();
            }

            LOGGER.info("Retrieving notifications by type: " + notificationType +
                    " for user: " + currentUser + " page: " + page + " size: " + size);

            //List<NotificationDTO> notifications = notifService.getNotificationsByType(
            //        currentUser, notificationType, page, size, includeRead, isAdmin);
            List<NotificationDTO> notifications = notifService.getNotificationsByType(notificationType);

            // Get total count for pagination metadata
            //long totalCount = notifService.getNotificationCountByType(
            //        currentUser, notificationType, includeRead, isAdmin);
            long totalCount = notifications.size();

            NotificationPageResponse response = new NotificationPageResponse(
                    notifications, page, size, totalCount);

            LOGGER.info("Retrieved " + notifications.size() + " notifications of type: " +
                    notificationType + " for user: " + currentUser);
            return Response
                    .ok(response)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid notification type request: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_TYPE", e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve notifications by type: " + type, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }
    }

    /**
     * Validates user notification request parameters
     */
    private ValidationResult validateUserNotificationRequest(String userId, int page, int size) {
        if (userId == null || userId.trim().isEmpty()) {
            return new ValidationResult(false, "User ID cannot be null or empty");
        }

        if (page < 0) {
            return new ValidationResult(false, "Page number must be non-negative");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            return new ValidationResult(false, "Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validates type notification request parameters
     */
    private ValidationResult validateTypeNotificationRequest(String type, int page, int size) {
        if (type == null || type.trim().isEmpty()) {
            return new ValidationResult(false, "Notification type cannot be null or empty");
        }

        if (page < 0) {
            return new ValidationResult(false, "Page number must be non-negative");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            return new ValidationResult(false, "Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        return new ValidationResult(true, null);
    }

    /**
     * Validation result container
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Paginated notification response
     */
    public static class NotificationPageResponse {
        private List<NotificationDTO> notifications;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public NotificationPageResponse(List<NotificationDTO> notifications, int page, int size, long totalElements) {
            this.notifications = notifications;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / size);
            this.hasNext = page < totalPages - 1;
            this.hasPrevious = page > 0;
        }

        // Getters
        public List<NotificationDTO> getNotifications() {
            return notifications;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }
    }
}
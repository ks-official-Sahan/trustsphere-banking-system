package com.trustsphere.rest.mapper;

import com.trustsphere.ejb.exception.AccountNotFoundException;
import com.trustsphere.ejb.exception.InsufficientFundsException;
import com.trustsphere.ejb.exception.UserNotFoundException;
import com.trustsphere.ejb.exception.BusinessException;
import com.trustsphere.ejb.exception.DataAccessException;
import com.trustsphere.ejb.exception.ValidationException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

@Provider
public class RestExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionMapper.class);

    @Context
    private UriInfo uriInfo;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public Response toResponse(Throwable exception) {
        // Generate correlation ID for error tracking
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Log exception with context
        logException(exception, correlationId);

        // Build error response based on an exception type
        ErrorResponse errorResponse = buildErrorResponse(exception, correlationId);
        Response.Status status = determineHttpStatus(exception);

        return Response.status(status)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .header("X-Correlation-ID", correlationId)
                .build();
    }

    private Response.Status determineHttpStatus(Throwable exception) {
        // Handle JAX-RS WebApplicationException first
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            return Response.Status.fromStatusCode(webEx.getResponse().getStatus());
        }

        // Handle business exceptions from EJB layer
        if (exception instanceof AccountNotFoundException) {
            return Response.Status.NOT_FOUND;
        }

        if (exception instanceof UserNotFoundException) {
            return Response.Status.NOT_FOUND;
        }

        if (exception instanceof InsufficientFundsException) {
            return Response.Status.CONFLICT;
        }

        if (exception instanceof SecurityException) {
            return Response.Status.FORBIDDEN;
        }

        if (exception instanceof ValidationException) {
            return Response.Status.BAD_REQUEST;
        }

        if (exception instanceof DataAccessException) {
            return Response.Status.SERVICE_UNAVAILABLE;
        }

        if (exception instanceof BusinessException) {
            // Generic business exception - could be client or server error
            BusinessException bizEx = (BusinessException) exception;
            return bizEx.isClientError() ? Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;
        }

        if (exception instanceof IllegalArgumentException ||
                exception instanceof IllegalStateException) {
            return Response.Status.BAD_REQUEST;
        }

        // Default to internal server error for unexpected exceptions
        return Response.Status.INTERNAL_SERVER_ERROR;
    }

    private ErrorResponse buildErrorResponse(Throwable exception, String correlationId) {
        Response.Status status = determineHttpStatus(exception);
        String message = sanitizeErrorMessage(exception);
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";
        String method = resourceInfo != null && resourceInfo.getResourceMethod() != null ?
                resourceInfo.getResourceMethod().getName() : "unknown";

        return ErrorResponse.builder()
                .error(getErrorCode(exception))
                .message(message)
                .status(status.getStatusCode())
                .timestamp(Instant.now().toString())
                .path(path)
                .method(method)
                .correlationId(correlationId)
                .build();
    }

    private String getErrorCode(Throwable exception) {
        if (exception instanceof AccountNotFoundException) {
            return "ACCOUNT_NOT_FOUND";
        }
        if (exception instanceof UserNotFoundException) {
            return "USER_NOT_FOUND";
        }
        if (exception instanceof InsufficientFundsException) {
            return "INSUFFICIENT_FUNDS";
        }
        if (exception instanceof SecurityException) {
            return "ACCESS_DENIED";
        }
        if (exception instanceof ValidationException) {
            return "VALIDATION_FAILED";
        }
        if (exception instanceof DataAccessException) {
            return "DATA_ACCESS_ERROR";
        }
        if (exception instanceof BusinessException) {
            return "BUSINESS_ERROR";
        }
        if (exception instanceof WebApplicationException) {
            return "WEB_APPLICATION_ERROR";
        }
        return "INTERNAL_ERROR";
    }

    private String sanitizeErrorMessage(Throwable exception) {
        String message = exception.getMessage();

        // Sanitize potentially sensitive information
        if (message == null) {
            message = "An error occurred processing your request";
        }

        // For security exceptions, provide generic message
        if (exception instanceof SecurityException) {
            return "Access denied - insufficient privileges";
        }

        // For internal errors, provide generic message in production
        if (isProductionEnvironment() && !(exception instanceof BusinessException)) {
            return "Internal server error - please contact support";
        }

        // Truncate very long error messages
        if (message.length() > 500) {
            message = message.substring(0, 497) + "...";
        }

        // Remove stack trace information that might leak internal details
        if (message.contains("at com.trustsphere") || message.contains("SQLException")) {
            return "An error occurred processing your request";
        }

        return message;
    }

    private void logException(Throwable exception, String correlationId) {
        Level logLevel = determineLogLevel(exception);
        String context = String.format("[%s] %s %s",
                correlationId,
                uriInfo != null ? uriInfo.getPath() : "unknown-path",
                resourceInfo != null ? resourceInfo.getResourceMethod() : "unknown-method");

        LOGGER.info("{}: REST Exception: {}", logLevel, context, exception);

        // Additional audit logging for security exceptions
        if (exception instanceof SecurityException) {
            LOGGER.warn("Security violation detected: {} - {}", context, exception.getMessage());
        }
    }

    private Level determineLogLevel(Throwable exception) {
        // Client errors (4xx) - log at INFO level
        if (exception instanceof AccountNotFoundException ||
                exception instanceof UserNotFoundException ||
                exception instanceof ValidationException ||
                exception instanceof IllegalArgumentException ||
                exception instanceof SecurityException) {
            return Level.INFO;
        }

        // Business conflicts - log at WARNING level
        if (exception instanceof InsufficientFundsException |
                exception instanceof BusinessException) {
            return Level.WARNING;
        }

        // Server errors (5xx) - log at SEVERE level
        return Level.SEVERE;
    }

    private boolean isProductionEnvironment() {
        String environment = System.getProperty("trustsphere.environment", "development");
        return "production".equalsIgnoreCase(environment);
    }

    public static class ErrorResponse {
        private String error;
        private String message;
        private int status;
        private String timestamp;
        private String path;
        private String method;
        private String correlationId;

        // Private constructor for a builder pattern
        private ErrorResponse() {}

        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public String getTimestamp() { return timestamp; }
        public String getPath() { return path; }
        public String getMethod() { return method; }
        public String getCorrelationId() { return correlationId; }

        public static class ErrorResponseBuilder {
            private final ErrorResponse errorResponse = new ErrorResponse();

            public ErrorResponseBuilder error(String error) {
                errorResponse.error = error;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                errorResponse.message = message;
                return this;
            }

            public ErrorResponseBuilder status(int status) {
                errorResponse.status = status;
                return this;
            }

            public ErrorResponseBuilder timestamp(String timestamp) {
                errorResponse.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder path(String path) {
                errorResponse.path = path;
                return this;
            }

            public ErrorResponseBuilder method(String method) {
                errorResponse.method = method;
                return this;
            }

            public ErrorResponseBuilder correlationId(String correlationId) {
                errorResponse.correlationId = correlationId;
                return this;
            }

            public ErrorResponse build() {
                return errorResponse;
            }
        }
    }

}
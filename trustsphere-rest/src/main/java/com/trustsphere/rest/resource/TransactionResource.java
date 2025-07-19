package com.trustsphere.rest.resource;

import com.trustsphere.ejb.api.TransactionServiceRemote;
import com.trustsphere.core.dto.TransactionDTO;

import com.trustsphere.rest.model.ErrorResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@RequestScoped
public class TransactionResource {

    private static final Logger LOGGER = Logger.getLogger(TransactionResource.class.getName());

    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("0.01");

    @EJB
    private TransactionServiceRemote txnService;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("transfer")
    @RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_TELLER"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Response transfer(
            @FormParam("srcId")
            @NotNull(message = "Source account ID is required")
            @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "Invalid source account ID format")
            String srcId,

            @FormParam("tgtId")
            @NotNull(message = "Target account ID is required")
            @Pattern(regexp = "^[A-Z0-9]{10,20}$", message = "Invalid target account ID format")
            String tgtId,

            @FormParam("amount")
            @NotNull(message = "Transfer amount is required")
            @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
            BigDecimal amount,

            @FormParam("description")
            String description) {

        try {
            // Input validation and sanitization
            ValidationResult validationResult = validateTransferRequest(srcId, tgtId, amount);
            if (!validationResult.isValid()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_FAILED", validationResult.getMessage()))
                        .build();
            }

            // Sanitize inputs
            srcId = sanitizeInput(srcId);
            tgtId = sanitizeInput(tgtId);
            description = sanitizeInput(description);

            String currentUser = securityContext.getUserPrincipal().getName();
            boolean isPrivilegedUser = securityContext.isUserInRole("ROLE_TELLER") ||
                    securityContext.isUserInRole("ROLE_ADMIN");

            LOGGER.info("Processing transfer: " + srcId + " -> " + tgtId +
                    " Amount: " + amount + " User: " + currentUser);

            // Execute atomic transfer
            //TransactionDTO completedTransaction = txnService.transfer(
            //        srcId, tgtId, amount, description, currentUser, isPrivilegedUser);
            TransactionDTO completedTransaction = txnService.transfer(
                    srcId, tgtId, amount);

            LOGGER.info("Transfer completed successfully: " + completedTransaction.getId());

            return Response
                    .status(Response.Status.CREATED)
                    .entity(completedTransaction)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid transfer request: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_TRANSFER_DATA", e.getMessage()))
                    .build();

        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Unauthorized transfer attempt: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("UNAUTHORIZED_TRANSFER", "Access denied"))
                    .build();

        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Transfer business rule violation: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("INSUFFICIENT_FUNDS", e.getMessage()))
                    .build();

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Transfer validation failed: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_MODIFIED)
                    .entity(new ErrorResponse("BUSINESS_RULE_VIOLATION", e.getMessage()))
                    .build();

        } catch (EJBTransactionRolledbackException e) {
            LOGGER.log(Level.SEVERE, "Transaction rolled back: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("TRANSACTION_FAILED", "Transfer could not be completed"))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during transfer: " + e.getMessage(), e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("PROCESSING_ERROR", "Internal server error"))
                    .build();
        }
    }

    /**
     * Validates transfer request parameters
     */
    private ValidationResult validateTransferRequest(String srcId, String tgtId, BigDecimal amount) {
        // Check for null or empty account IDs
        if (srcId == null || srcId.trim().isEmpty()) {
            return new ValidationResult(false, "Source account ID cannot be null or empty");
        }

        if (tgtId == null || tgtId.trim().isEmpty()) {
            return new ValidationResult(false, "Target account ID cannot be null or empty");
        }

        // Prevent self-transfer
        if (srcId.equals(tgtId)) {
            return new ValidationResult(false, "Source and target accounts cannot be the same");
        }

        // Validate amount
        if (amount == null) {
            return new ValidationResult(false, "Transfer amount is required");
        }

        if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            return new ValidationResult(false, "Transfer amount must be at least " + MIN_TRANSFER_AMOUNT);
        }

        if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            return new ValidationResult(false, "Transfer amount cannot exceed " + MAX_TRANSFER_AMOUNT);
        }

        // Validate decimal precision (max 2 decimal places for currency)
        if (amount.scale() > 2) {
            return new ValidationResult(false, "Transfer amount cannot have more than 2 decimal places");
        }

        return new ValidationResult(true, null);
    }

    /**
     * Sanitizes user input to prevent injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
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

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
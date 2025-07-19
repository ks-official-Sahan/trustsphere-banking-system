package com.trustsphere.rest.resource;

import com.trustsphere.ejb.exception.AccountNotFoundException;
import com.trustsphere.ejb.remote.TransactionServiceRemote;
import com.trustsphere.core.dto.TransactionDTO;

import com.trustsphere.rest.model.ErrorResponse;
import com.trustsphere.rest.model.TransferRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("0.01");

    @EJB
    private TransactionServiceRemote txnService;

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("{accId}")
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getTransactionsBySourceAccount(@PathParam("accId") String sourceAccount) {
        try {
            List<TransactionDTO> transactions = txnService.getTransactionsBySourceAccount(sourceAccount);
            LOGGER.info("Retrieved {} transactions from account: {}", transactions.size(), sourceAccount);
            return Response
                    .ok(transactions)
                    .build();

        } catch (AccountNotFoundException e) {
            LOGGER.warn("Account not found: {} : {}", sourceAccount, e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("ACCOUNT_NOT_FOUND", "Account with ID " + sourceAccount + " not found"))
                    .build();

        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized account access attempt: {}", sourceAccount, e);
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("UNAUTHORIZED_ACCESS", "Access denied"))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to retrieve transactions from account: {}", sourceAccount, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }

    }

    @POST
    @Path("transfer")
    @RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_TELLER"})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Response transfer(@Valid TransferRequest req) {
        try {
            // Input validation and sanitization
            ValidationResult validationResult = validateTransferRequest(req.srcId, req.tgtId, req.amount);
            if (!validationResult.isValid()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("VALIDATION_FAILED", validationResult.getMessage()))
                        .build();
            }

            String currentUser = securityContext.getUserPrincipal().getName();
            boolean isPrivilegedUser = securityContext.isUserInRole("ROLE_TELLER") ||
                    securityContext.isUserInRole("ROLE_ADMIN");

            LOGGER.info("Processing transfer: {} -> {} Amount: {} User: {}", req.srcId, req.tgtId, req.amount, currentUser);

            // Execute atomic transfer
            TransactionDTO completedTransaction = txnService.transfer(
                    req.srcId, req.tgtId, req.amount);
            // TransactionDTO completedTransaction = txnService.transfer(
            // req.srcId, req.tgtId, req.amount, req.description, req.currentUser, req.isPrivilegedUser);

            LOGGER.info("Transfer completed successfully: {}", completedTransaction.getId());

            return Response
                    .status(Response.Status.CREATED)
                    .entity(completedTransaction)
                    .build();

        } catch (
                IllegalArgumentException e) {
            LOGGER.warn("Invalid transfer request: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_TRANSFER_DATA", e.getMessage()))
                    .build();

        } catch (
                SecurityException e) {
            LOGGER.warn("Unauthorized transfer attempt: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("UNAUTHORIZED_TRANSFER", "Access denied"))
                    .build();

        } catch (
                IllegalStateException e) {
            LOGGER.warn("Transfer business rule violation: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("INSUFFICIENT_FUNDS", e.getMessage()))
                    .build();

        } catch (
                ValidationException e) {
            LOGGER.warn("Transfer validation failed: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_MODIFIED)
                    .entity(new ErrorResponse("BUSINESS_RULE_VIOLATION", e.getMessage()))
                    .build();

        } catch (
                EJBTransactionRolledbackException e) {
            LOGGER.error("Transaction rolled back: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("TRANSACTION_FAILED", "Transfer could not be completed"))
                    .build();

        } catch (
                Exception e) {
            LOGGER.error("Unexpected error during transfer: {}", e.getMessage(), e);
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

        if (amount.scale() > 2) {
            return new ValidationResult(false, "Transfer amount cannot have more than 2 decimal places");
        }

        return new ValidationResult(true, null);
    }

    // Validation result container
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
}
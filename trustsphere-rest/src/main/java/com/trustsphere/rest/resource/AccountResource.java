package com.trustsphere.rest.resource;

import com.trustsphere.core.enums.AccountStatus;
import com.trustsphere.ejb.remote.AccountServiceRemote;
import com.trustsphere.core.dto.AccountDTO;

import com.trustsphere.ejb.exception.AccountNotFoundException;
import com.trustsphere.rest.model.ErrorResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);

    @EJB
    private AccountServiceRemote accountService;

    @Context
    private SecurityContext securityContext;

    @POST
    @RolesAllowed({"ROLE_TELLER", "ROLE_ADMIN"})
    public Response createAccount(@Valid @NotNull AccountDTO dto) {
        try {
            String currentUser = securityContext.getUserPrincipal().getName();
            LOGGER.info("Creating account for user: " + dto.getUserId() + " by: " + currentUser);

            AccountDTO createdAccount = accountService.createAccount(dto);

            LOGGER.info("Account created successfully: " + createdAccount.getAccountNumber());
            return Response
                    .status(Response.Status.CREATED)
                    .entity(createdAccount)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid account data: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_ACCOUNT_DATA", e.getMessage()))
                    .build();

        } catch (IllegalStateException e) {
            LOGGER.warn("Account creation rule violation: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("BUSINESS_RULE_VIOLATION", e.getMessage()))
                    .build();

        } catch (jakarta.validation.ValidationException e) {
            LOGGER.warn("Account validation failed: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_MODIFIED)
                    .entity(new ErrorResponse("VALIDATION_FAILED", e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.warn("Failed to create account: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("CREATION_FAILED", "Internal server error"))
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"ROLE_USER", "ROLE_TELLER", "ROLE_ADMIN"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getAccountById(
            @PathParam("id") String id) {

        try {
            if (id == null || id.trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_ID", "Account ID cannot be null or empty"))
                        .build();
            }

            //String currentUser = securityContext.getUserPrincipal().getName();
            //boolean isPrivilegedUser = securityContext.isUserInRole("ROLE_TELLER") ||
            //        securityContext.isUserInRole("ROLE_ADMIN");

            AccountDTO account = accountService.getAccountById(id);
            //AccountDTO account = accountService.getAccountById(id, currentUser, isPrivilegedUser);

            if (account == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("ACCOUNT_NOT_FOUND", "Account with ID " + id + " not found"))
                        .build();
            }

            return Response
                    .ok(account)
                    .build();

        } catch (AccountNotFoundException e) {
            LOGGER.warn("Account not found: {} : {}", id, e.getMessage(), e);
            return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("ACCOUNT_NOT_FOUND", "Account with ID " + id + " not found"))
                    .build();

        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized account access attempt: {}", id, e);
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("UNAUTHORIZED_ACCESS", "Access denied"))
                    .build();

        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve account: {}", id, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }
    }

    @GET
    @Path("user/{userId}")
    @RolesAllowed({"ROLE_USER", "ROLE_TELLER", "ROLE_ADMIN"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response listActiveByUser(
            @PathParam("userId") String userId) {

        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_USER_ID", "User ID cannot be null or empty"))
                        .build();
            }

            String currentUser = securityContext.getUserPrincipal().getName();
            boolean isPrivilegedUser = securityContext.isUserInRole("ROLE_TELLER") ||
                    securityContext.isUserInRole("ROLE_ADMIN");

            // Enforce data isolation - users can only see their own accounts unless privileged
            if (!isPrivilegedUser && !currentUser.equals(userId)) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("UNAUTHORIZED_ACCESS", "Access denied"))
                        .build();
            }

            List<AccountDTO> activeAccounts = accountService.listActiveByUser(userId);

            LOGGER.info("Retrieved " + activeAccounts.size() + " active accounts for user: " + userId);
            return Response
                    .ok(activeAccounts)
                    .build();

        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized user account access attempt: {}", userId, e);
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("UNAUTHORIZED_ACCESS", "Access denied"))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid user ID: {}", userId, e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("USER_NOT_FOUND", "User not found"))
                    .build();

        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve user accounts: {}", userId, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }
    }

    @PUT
    @Path("{id}/status")
    @RolesAllowed({"ROLE_TELLER", "ROLE_ADMIN"})
    public Response updateStatus(
            @PathParam("id") String id,
            @QueryParam("status") AccountStatus status,
            @QueryParam("reason") String reason) {

        try {
            if (id == null || id.trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_ID", "Account ID cannot be null or empty"))
                        .build();
            }

            if (status == null) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_STATUS", "Status parameter is required"))
                        .build();
            }

            String currentUser = securityContext.getUserPrincipal().getName();
            LOGGER.info("Updating account status: {} to {} by: {}", id, status, currentUser);

            //boolean updated = accountService.updateStatus(id, status, reason, currentUser);
            accountService.updateStatus(id, status);

            LOGGER.info("Account status updated successfully: {} to {}", id, status);
            return Response
                    .noContent()
                    .build();

        } catch (AccountNotFoundException e) {
            LOGGER.warn("Account not found: {} : {}", id, e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("ACCOUNT_NOT_FOUND", "Account with ID " + id + " not found"))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid status update request: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_STATUS", e.getMessage()))
                    .build();

        } catch (IllegalStateException e) {
            LOGGER.warn("Invalid status transition: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_MODIFIED)
                    .entity(new ErrorResponse("INVALID_TRANSITION", e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.warn("Failed to update account status: {}", id, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("UPDATE_FAILED", "Internal server error"))
                    .build();
        }
    }

}
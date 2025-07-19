package com.trustsphere.rest.resource;

import com.trustsphere.ejb.remote.UserServiceRemote;
import com.trustsphere.core.dto.UserDTO;

import com.trustsphere.core.enums.UserStatus;

import com.trustsphere.ejb.exception.UserNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    @EJB
    private UserServiceRemote userService;

    @POST
    @RolesAllowed("ROLE_ADMIN")
    public Response createUser(@Valid @NotNull UserDTO dto) {
        try {
            LOGGER.info("Creating user: {}", dto.getEmail());

            UserDTO createdUser = userService.createUser(dto);

            LOGGER.info("User created successfully: {}", createdUser.getId());
            return Response
                    .status(Response.Status.CREATED)
                    .entity(createdUser)
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid user data: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_USER_DATA", e.getMessage()))
                    .build();

        } catch (IllegalStateException e) {
            LOGGER.warn("User already exists: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("USER_EXISTS", e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to create user: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("CREATION_FAILED", "Internal server error"))
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response getUserById(
            @PathParam("id") String id) {

        try {
            if (id == null || id.trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_ID", "User ID cannot be null or empty"))
                        .build();
            }

            UserDTO user = userService.getUserById(id);

            return Response
                    .ok(user)
                    .build();

        } catch (UserNotFoundException e) {
            LOGGER.warn("User not found with id: {} : {}", id, e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("USER_NOT_FOUND", "User with ID " + id + " not found"))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to retrieve user: {}", id, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }
    }

    @GET
    @RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Response listActiveUsers() {
        try {
            List<UserDTO> activeUsers = userService.listActiveUsers();

            LOGGER.info("Retrieved {} active users", activeUsers.size());
            return Response
                    .ok(activeUsers)
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to retrieve active users", e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("RETRIEVAL_FAILED", "Internal server error"))
                    .build();
        }
    }

    @PUT
    @Path("{id}/status")
    @RolesAllowed("ROLE_ADMIN")
    public Response updateStatus(
            @PathParam("id") String id,
            @QueryParam("status") UserStatus status) {

        try {
            if (id == null || id.trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_ID", "User ID cannot be null or empty"))
                        .build();
            }

            if (status == null) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("INVALID_STATUS", "Status parameter is required"))
                        .build();
            }

            LOGGER.info("Updating user status: {} to {}", id, status);

            userService.updateStatus(id, status);

            LOGGER.info("User status updated successfully: {}", id);
            return Response
                    .noContent()
                    .build();

        } catch (UserNotFoundException e) {
            LOGGER.warn("User not found with id: {} : {}", id, e.getMessage(), e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("USER_NOT_FOUND", "User with ID " + id + " not found"))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid status update request: {}", e.getMessage(), e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_STATUS", e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Failed to update user status: {}", id, e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("UPDATE_FAILED", "Internal server error"))
                    .build();
        }
    }
}
package com.trustsphere.rest.resource;

import com.trustsphere.core.dto.LoginRequestDTO;
import com.trustsphere.core.dto.LoginResponseDTO;
import com.trustsphere.core.dto.RefreshTokenRequestDTO;
import com.trustsphere.ejb.exception.AuthenticationException;
import com.trustsphere.ejb.remote.AuthServiceRemote;
import com.trustsphere.rest.model.ErrorResponse;
import com.trustsphere.rest.security.RateLimitFilter;

import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS Resource for Authentication API
 * Provides endpoints for login, token refresh, and logout operations
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);

    @EJB
    private AuthServiceRemote authService;

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequestDTO loginRequest) {
        LOGGER.info("Login request received for email: {}", loginRequest.getEmail());

        try {
            LoginResponseDTO response = authService.login(loginRequest);
            
            LOGGER.info("Login successful for user: {}", loginRequest.getEmail());
            return Response.ok(response)
                    .header("Cache-Control", "no-store")
                    .header("Pragma", "no-cache")
                    .build();

        } catch (AuthenticationException e) {
            LOGGER.warn("Login failed for user {}: {} - {}", 
                       loginRequest.getEmail(), e.getErrorCode(), e.getMessage());
            
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(e.getErrorCode(), e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Unexpected error during login for user: {}", loginRequest.getEmail(), e);
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("LOGIN_ERROR", "An unexpected error occurred during login"))
                    .build();
        }
    }

    /**
     * Token refresh endpoint
     * POST /api/auth/refresh
     */
    @POST
    @Path("/refresh")
    public Response refreshToken(@Valid RefreshTokenRequestDTO refreshRequest) {
        LOGGER.info("Token refresh request received");

        try {
            LoginResponseDTO response = authService.refreshToken(refreshRequest);
            
            LOGGER.info("Token refresh successful");
            return Response.ok(response)
                    .header("Cache-Control", "no-store")
                    .header("Pragma", "no-cache")
                    .build();

        } catch (AuthenticationException e) {
            LOGGER.warn("Token refresh failed: {} - {}", e.getErrorCode(), e.getMessage());
            
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(e.getErrorCode(), e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.error("Unexpected error during token refresh", e);
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("REFRESH_ERROR", "An unexpected error occurred during token refresh"))
                    .build();
        }
    }

    /**
     * User logout endpoint
     * POST /api/auth/logout
     */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authorizationHeader) {
        LOGGER.info("Logout request received");

        try {
            // Extract refresh token from request body or use access token for logging
            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }

            authService.logout(token);
            
            LOGGER.info("Logout successful");
            return Response.ok()
                    .entity("{\"message\":\"Logout successful\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.warn("Error during logout: {}", e.getMessage());
            
            // Logout should always succeed from client perspective
            return Response.ok()
                    .entity("{\"message\":\"Logout completed\"}")
                    .build();
        }
    }

    /**
     * Health check endpoint for authentication service
     * GET /api/auth/health
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        try {
            // Simple health check - verify service is available
            boolean isHealthy = authService != null;
            
            if (isHealthy) {
                return Response.ok()
                        .entity("{\"status\":\"UP\",\"service\":\"AuthAPI\"}")
                        .build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("{\"status\":\"DOWN\",\"service\":\"AuthAPI\"}")
                        .build();
            }
            
        } catch (Exception e) {
            LOGGER.error("Health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"status\":\"DOWN\",\"service\":\"AuthAPI\",\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Validate user endpoint (for internal use)
     * GET /api/auth/validate/{email}
     */
    @GET
    @Path("/validate/{email}")
    public Response validateUser(@PathParam("email") String email) {
        LOGGER.info("User validation request for email: {}", email);

        try {
            boolean isActive = authService.isUserActiveByEmail(email);
            
            return Response.ok()
                    .entity("{\"email\":\"" + email + "\",\"active\":" + isActive + "}")
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error validating user: {}", email, e);
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("VALIDATION_ERROR", "Error validating user"))
                    .build();
        }
    }
}
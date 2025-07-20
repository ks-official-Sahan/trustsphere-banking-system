package com.trustsphere.rest.mapper;

import com.trustsphere.ejb.exception.AuthenticationException;
import com.trustsphere.rest.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationExceptionMapper.class);

    @Override
    public Response toResponse(AuthenticationException exception) {
        LOGGER.warn("Authentication exception: {} - {}", exception.getErrorCode(), exception.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                exception.getErrorCode() != null ? exception.getErrorCode() : "AUTHENTICATION_ERROR",
                exception.getMessage()
        );

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(errorResponse)
                .header("WWW-Authenticate", "Bearer realm=\"TrustSphere\"")
                .build();
    }
}
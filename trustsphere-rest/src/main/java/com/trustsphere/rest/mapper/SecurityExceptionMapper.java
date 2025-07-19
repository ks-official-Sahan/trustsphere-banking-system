package com.trustsphere.rest.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

    @Override
    public Response toResponse(SecurityException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\":\"access_denied\",\"message\":\"" + exception.getMessage() + "\"}")
                .type("application/json")
                .build();
    }
}

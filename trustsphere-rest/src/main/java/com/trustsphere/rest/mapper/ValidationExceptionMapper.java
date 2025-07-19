package com.trustsphere.rest.mapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        StringBuilder message = new StringBuilder();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            message.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"validation_failed\",\"message\":\"" + message.toString() + "\"}")
                .type("application/json")
                .build();
    }
}

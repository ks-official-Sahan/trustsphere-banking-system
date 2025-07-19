package com.trustsphere.rest.provider;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerResponseFilter {

    private static final String ALLOWED_ORIGINS = "*";
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type, Accept, X-Requested-With, Origin";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH";
    private static final String MAX_AGE = "86400"; // 24 hours

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(Response.Status.OK.getStatusCode());
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", ALLOWED_ORIGINS);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", ALLOWED_HEADERS);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", ALLOWED_METHODS);
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", MAX_AGE);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().putSingle("Access-Control-Expose-Headers", "Authorization");
    }}

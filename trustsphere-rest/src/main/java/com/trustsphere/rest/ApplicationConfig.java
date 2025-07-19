package com.trustsphere.rest;

import com.trustsphere.rest.mapper.SecurityExceptionMapper;
import com.trustsphere.rest.mapper.ValidationExceptionMapper;
import com.trustsphere.rest.resource.*;
import com.trustsphere.rest.security.JWTAuthenticationFilter;
import com.trustsphere.rest.provider.CorsFilter;
import com.trustsphere.rest.mapper.RestExceptionMapper;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                // Security Infrastructure - Priority 1000
                JWTAuthenticationFilter.class,

                // Business Domain Resources - Priority 500
                AccountResource.class,
                AuditResource.class,
                NotificationResource.class,
                TransactionResource.class,
                UserResource.class,

                // Cross-Cutting Infrastructure - Priority 100
                CorsFilter.class,
                RestExceptionMapper.class,
                ValidationExceptionMapper.class,
                SecurityExceptionMapper.class
        );
    }
}
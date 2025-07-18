package com.trustsphere.ejb.interceptor;

import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.ejb.dao.AuditLogDAO;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.time.Instant;

@Interceptor
@AuditLogged
@Priority(Interceptor.Priority.APPLICATION)
public class AuditInterceptor {

    @Inject
    private AuditLogDAO auditLogDAO;

    @AroundInvoke
    public Object logAudit(InvocationContext ctx) throws Exception {
        Instant start = Instant.now();

        Object result = ctx.proceed();

        AuditLog log = new AuditLog();
        log.setActorUserId("system"); // Can be injected from context
        log.setAction(ctx.getMethod().getName());
        log.setResourceType(ctx.getTarget().getClass().getSimpleName());
        log.setResourceId("N/A");
        log.setSeverityLevel(SeverityLevel.INFO);
        log.setDetails("Invoked: " + ctx.getMethod().getName());
        log.setTimestamp(start);
        log.setIpAddress("127.0.0.1"); // Replace with actual IP if available
        log.setUserAgent("ejb-service");

        auditLogDAO.save(log);

        return result;
    }
}

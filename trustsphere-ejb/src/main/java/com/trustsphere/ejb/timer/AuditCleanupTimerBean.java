package com.trustsphere.ejb.timer;

import com.trustsphere.ejb.service.AuditServiceBean;

import jakarta.ejb.*;
import jakarta.inject.Inject;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AuditCleanupTimerBean {

    @Inject
    private AuditServiceBean auditService;

    @Schedule(dayOfWeek = "Sun", hour = "3", minute = "0", persistent = false)
    public void cleanup() {
        auditService.deleteOlderThan(30); // Deletes logs older than 30 days
    }
}

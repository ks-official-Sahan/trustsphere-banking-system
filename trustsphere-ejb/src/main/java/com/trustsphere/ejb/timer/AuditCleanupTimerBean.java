package com.trustsphere.ejb.timer;

import com.trustsphere.ejb.remote.AuditServiceRemote;

import jakarta.ejb.*;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AuditCleanupTimerBean {

    @EJB
    private AuditServiceRemote auditService;

    @Schedule(dayOfWeek = "Sun", hour = "3", minute = "0", persistent = false)
    public void cleanup() {
        auditService.deleteOlderThan(30); // Deletes logs older than 30 days
    }
}

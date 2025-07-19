package com.trustsphere.ejb.remote;

import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.core.dto.AuditLogDTO;

import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface AuditServiceRemote {

    List<AuditLogDTO> getRecentLogs(int limit);

    List<AuditLogDTO> getLogsBySeverity(SeverityLevel level, int offset, int limit);

    List<AuditLogDTO> getLogsBySeverity(SeverityLevel level);

    List<AuditLogDTO> getLogsByUser(String userId, int offset, int limit);

    List<AuditLogDTO> getLogsByUser(String userId);

    List<AuditLogDTO> getLogsByResource(String resourceType, String resourceId, int offset, int limit);

    List<AuditLogDTO> getLogsByResource(String resourceType, String resourceId);

    void recordAuditEntry(AuditLogDTO auditLogDTO);

    void deleteOlderThan(int days);

}

package com.trustsphere.ejb.api;

import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.ejb.dto.AuditLogDTO;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface AuditServiceRemote {

    List<AuditLogDTO> getBySeverity(SeverityLevel level);

    List<AuditLogDTO> getRecent(int max);
}
package com.trustsphere.ejb.service;

import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.ejb.api.AuditServiceRemote;
import com.trustsphere.ejb.dao.AuditLogDAO;
import com.trustsphere.ejb.dto.AuditLogDTO;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class AuditServiceBean implements AuditServiceRemote {

    @EJB
    private AuditLogDAO auditLogDAO;

    @Override
    public List<AuditLogDTO> getBySeverity(SeverityLevel level) {
        return auditLogDAO.findBySeverity(level).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getRecent(int max) {
        return auditLogDAO.findRecent(max).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());
        dto.setActorUserId(auditLog.getActorUserId());
        dto.setAction(auditLog.getAction());
        dto.setResourceType(auditLog.getResourceType());
        dto.setResourceId(auditLog.getResourceId());
        dto.setSeverityLevel(auditLog.getSeverityLevel());
        dto.setDetails(auditLog.getDetails());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }
}
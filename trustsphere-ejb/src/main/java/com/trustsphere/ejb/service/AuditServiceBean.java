package com.trustsphere.ejb.service;

import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.SeverityLevel;
import com.trustsphere.ejb.remote.AuditServiceRemote;
import com.trustsphere.ejb.dao.AuditLogDAO;
import com.trustsphere.core.dto.AuditLogDTO;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Stateless
@RolesAllowed({"ROLE_AUDITOR", "ROLE_ADMIN"})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AuditServiceBean implements AuditServiceRemote {

    @Inject
    private AuditLogDAO auditLogDAO;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AuditLogDTO> getRecentLogs(int limit) {
        return auditLogDAO.findRecent(limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getLogsBySeverity(SeverityLevel level, int offset, int limit) {
        return auditLogDAO.findBySeverity(level, offset, limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AuditLogDTO> getLogsBySeverity(SeverityLevel level) {
        return getLogsBySeverity(level, 0, 1000);
    }

    @Override
    public List<AuditLogDTO> getLogsByUser(String userId, int offset, int limit) {
        return auditLogDAO.findByUserId(userId, offset, limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AuditLogDTO> getLogsByUser(String userId) {
        return getLogsByUser(userId, 0, 1000);
    }

    @Override
    public List<AuditLogDTO> getLogsByResource(String resourceType, String resourceId, int offset, int limit) {
        return auditLogDAO.findByResource(resourceType, resourceId, offset, limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AuditLogDTO> getLogsByResource(String resourceType, String resourceId) {
        return getLogsByResource(resourceType, resourceId, 0, 1000);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recordAuditEntry(AuditLogDTO dto) {
        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID().toString());
        log.setActorUserId(dto.getActorUserId());
        log.setAction(dto.getAction());
        log.setResourceType(dto.getResourceType());
        log.setResourceId(dto.getResourceId());
        log.setSeverityLevel(dto.getSeverityLevel());
        log.setDetails(dto.getDetails());
        log.setIpAddress(dto.getIpAddress());
        log.setUserAgent(dto.getUserAgent());
        log.setTimestamp(dto.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
        auditLogDAO.save(log);
    }

    private AuditLogDTO mapToDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(log.getId());
        dto.setActorUserId(log.getActorUserId());
        dto.setAction(log.getAction());
        dto.setResourceType(log.getResourceType());
        dto.setResourceId(log.getResourceId());
        dto.setSeverityLevel(log.getSeverityLevel());
        dto.setDetails(log.getDetails());
        dto.setIpAddress(log.getIpAddress());
        dto.setUserAgent(log.getUserAgent());
        dto.setTimestamp(log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
        return dto;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void deleteOlderThan(int days) {
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        auditLogDAO.deleteBefore(threshold);
    }

}

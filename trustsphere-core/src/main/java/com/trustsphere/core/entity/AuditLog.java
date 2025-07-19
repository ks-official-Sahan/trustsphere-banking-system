package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.SeverityLevel;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;

@NamedQueries({
        @NamedQuery(
                name = "AuditLog.findByUserId",
                query = "SELECT a FROM AuditLog a WHERE a.actorUserId = :userId ORDER BY a.timestamp DESC"
        ),
        @NamedQuery(
                name = "AuditLog.findBySeverity",
                query = "SELECT a FROM AuditLog a WHERE a.severityLevel = :severityLevel ORDER BY a.timestamp DESC"
        ),
        @NamedQuery(
                name = "AuditLog.findByResource",
                query = "SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId"
        ),
        @NamedQuery(
                name = "AuditLog.findRecent",
                query = "SELECT a FROM AuditLog a ORDER BY a.timestamp DESC"
        )
})
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseAuditEntity implements Serializable {

    @NotNull
    @Column(name = "actor_user_id", nullable = false)
    private String actorUserId;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 20)
    private SeverityLevel severityLevel;

    @Size(max = 500)
    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public AuditLog() {}

    public AuditLog(String actorUserId, String action, String resourceType,
                    SeverityLevel severityLevel) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.resourceType = resourceType;
        this.severityLevel = severityLevel;
        this.timestamp = Instant.now();
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(String actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
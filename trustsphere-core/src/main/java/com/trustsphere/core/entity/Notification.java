package com.trustsphere.core.entity;

import com.trustsphere.core.entity.base.BaseAuditEntity;
import com.trustsphere.core.enums.NotificationType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;

@NamedQueries({
        @NamedQuery(
                name = "Notification.findByUserId",
                query = "SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.timestamp DESC"
        ),
        @NamedQuery(
                name = "Notification.findByType",
                query = "SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.timestamp DESC"
        )
})
@Entity
@Table(name = "notifications")
public class Notification extends BaseAuditEntity implements Serializable {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "message", nullable = false)
    private String message;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

package com.trustsphere.core.dto;

import com.trustsphere.core.enums.NotificationType;

import java.io.Serializable;
import java.time.Instant;

public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private NotificationType type;
    private String message;
    private Instant timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

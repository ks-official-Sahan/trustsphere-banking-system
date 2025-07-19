package com.trustsphere.ejb.api;

import com.trustsphere.core.dto.NotificationDTO;
import com.trustsphere.core.enums.NotificationType;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface NotificationServiceRemote {
    List<NotificationDTO> getNotificationsByUser(String userId);
    List<NotificationDTO> getNotificationsByType(NotificationType type);
}

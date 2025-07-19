package com.trustsphere.ejb.remote;

import com.trustsphere.core.dto.NotificationDTO;
import com.trustsphere.core.enums.NotificationType;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface NotificationServiceRemote {
    List<NotificationDTO> getNotificationsByUser(String userId);
    List<NotificationDTO> getNotificationsByUser(String userId, int offset, int limit);

    List<NotificationDTO> getNotificationsByType(NotificationType type);
    List<NotificationDTO> getNotificationsByType(NotificationType type, int offset, int limit);
}

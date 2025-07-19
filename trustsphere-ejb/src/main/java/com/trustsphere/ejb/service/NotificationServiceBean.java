package com.trustsphere.ejb.service;

import com.trustsphere.ejb.remote.NotificationServiceRemote;
import com.trustsphere.ejb.dao.NotificationDAO;
import com.trustsphere.core.dto.NotificationDTO;
import com.trustsphere.core.entity.Notification;
import com.trustsphere.core.enums.NotificationType;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
@RolesAllowed({"ROLE_USER", "ROLE_ADMIN", "ROLE_TELLER"})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class NotificationServiceBean implements NotificationServiceRemote {

    @EJB
    private NotificationDAO notificationDAO;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<NotificationDTO> getNotificationsByUser(String userId) {
        return getNotificationsByUser(userId, 0, 1000);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<NotificationDTO> getNotificationsByType(NotificationType type) {
        return getNotificationsByType(type, 0, 1000);
    }

    @Override
    public List<NotificationDTO> getNotificationsByUser(String userId, int offset, int limit) {
        return notificationDAO.findByUserId(userId, offset, limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsByType(NotificationType type, int offset, int limit) {
        return notificationDAO.findByType(type, offset, limit).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private NotificationDTO mapToDTO(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType());
        dto.setMessage(entity.getMessage());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }
}

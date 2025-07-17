package com.trustsphere.ejb.service;

import com.trustsphere.ejb.api.NotificationServiceRemote;
import com.trustsphere.ejb.dao.NotificationDAO;
import com.trustsphere.ejb.dto.NotificationDTO;
import com.trustsphere.core.entity.Notification;
import com.trustsphere.core.enums.NotificationType;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class NotificationServiceBean implements NotificationServiceRemote {

    @EJB
    private NotificationDAO notificationDAO;

    @Override
    public List<NotificationDTO> getNotificationsByUser(String userId) {
        return notificationDAO.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getNotificationsByType(NotificationType type) {
        return notificationDAO.findByType(type).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private NotificationDTO mapToDTO(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType());
        dto.setMessage(entity.getMessage());
        dto.setTimestamp(LocalDateTime.ofInstant(entity.getTimestamp(), ZoneId.systemDefault()));
        return dto;
    }
}

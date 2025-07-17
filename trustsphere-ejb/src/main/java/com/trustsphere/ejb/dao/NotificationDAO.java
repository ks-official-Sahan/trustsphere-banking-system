package com.trustsphere.ejb.dao;

import com.trustsphere.core.entity.Notification;
import com.trustsphere.core.enums.NotificationType;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.util.List;

@Stateless
public class NotificationDAO {

    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager em;

    public Notification create(Notification notification) {
        em.persist(notification);
        return notification;
    }

    public List<Notification> findByUserId(String userId) {
        TypedQuery<Notification> query = em.createNamedQuery("Notification.findByUserId", Notification.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<Notification> findByType(NotificationType type) {
        TypedQuery<Notification> query = em.createNamedQuery("Notification.findByType", Notification.class);
        query.setParameter("type", type);
        return query.getResultList();
    }
}

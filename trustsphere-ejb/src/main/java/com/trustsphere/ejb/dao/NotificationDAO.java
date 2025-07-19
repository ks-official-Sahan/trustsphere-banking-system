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

    public List<Notification> findByUserId(String userId, int offset, int limit) {
        TypedQuery<Notification> query = em.createNamedQuery("Notification.findByUserId", Notification.class).setFirstResult(offset).setMaxResults(limit);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<Notification> findByType(NotificationType type, int offset, int limit) {
        TypedQuery<Notification> query = em.createNamedQuery("Notification.findByType", Notification.class).setFirstResult(offset).setMaxResults(limit);
        query.setParameter("type", type);
        return query.getResultList();
    }
}

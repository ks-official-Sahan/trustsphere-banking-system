package com.trustsphere.ejb.dao;

import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.SeverityLevel;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class AuditLogDAO {

    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager em;

    public AuditLog create(AuditLog auditLog) {
        em.persist(auditLog);
        return auditLog;
    }

    public List<AuditLog> findBySeverity(SeverityLevel severity) {
        TypedQuery<AuditLog> query = em.createNamedQuery("AuditLog.findBySeverity", AuditLog.class);
        query.setParameter("severityLevel", severity);
        return query.getResultList();
    }

    public AuditLog findById(String id) {
        return em.find(AuditLog.class, id);
    }

    public List<AuditLog> findRecent(int max) {
        TypedQuery<AuditLog> query = em.createNamedQuery("AuditLog.findRecent", AuditLog.class);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public List<AuditLog> findByUserId(String userId, int max) {
        TypedQuery<AuditLog> query = em.createNamedQuery("AuditLog.findByUserId", AuditLog.class);
        query.setParameter("userId", userId);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public void delete(AuditLog auditLog) {
        if (em.contains(auditLog)) {
            em.remove(auditLog);
        } else {
            em.remove(em.merge(auditLog));
        }
    }
}
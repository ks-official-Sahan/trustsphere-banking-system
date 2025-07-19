package com.trustsphere.ejb.dao;

import com.trustsphere.core.entity.AuditLog;
import com.trustsphere.core.enums.SeverityLevel;

import java.time.Instant;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class AuditLogDAO {

    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager em;

    public EntityManager getEm() {
        return em;
    }

    public AuditLog create(AuditLog auditLog) {
        em.persist(auditLog);
        return auditLog;
    }

    public List<AuditLog> findRecent(int limit) {
        return em.createNamedQuery("AuditLog.findRecent", AuditLog.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> findBySeverity(SeverityLevel level, int offset, int limit) {
        return em.createNamedQuery("AuditLog.findBySeverity", AuditLog.class)
                .setParameter("severityLevel", level)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> findByUserId(String userId, int offset, int limit) {
        return em.createNamedQuery("AuditLog.findByUserId", AuditLog.class)
                .setParameter("userId", userId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> findByResource(String type, String resourceId, int offset, int limit) {
        return em.createNamedQuery("AuditLog.findByResource", AuditLog.class)
                .setParameter("resourceType", type)
                .setParameter("resourceId", resourceId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public void save(AuditLog log) {
        em.persist(log);
    }

    public void deleteBefore(Instant timestamp) {
        em.createQuery("DELETE FROM AuditLog a WHERE a.timestamp < :ts")
                .setParameter("ts", timestamp)
                .executeUpdate();
    }


    public void delete(AuditLog auditLog) {
        if (em.contains(auditLog)) {
            em.remove(auditLog);
        } else {
            em.remove(em.merge(auditLog));
        }
    }
}
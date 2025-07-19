package com.trustsphere.ejb.dao;

import com.trustsphere.core.entity.Transaction;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class TransactionDAO {

    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager em;

    public Transaction create(Transaction transaction) {
        em.persist(transaction);
        return transaction;
    }

    public Transaction findById(String id) {
        return em.find(Transaction.class, id);
    }

    public List<Transaction> findRecent(int max) {
        TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findRecent", Transaction.class);
        query.setMaxResults(max);
        return query.getResultList();
    }

    public Transaction update(Transaction transaction) {
        return em.merge(transaction);
    }

    public void delete(Transaction transaction) {
        if (em.contains(transaction)) {
            em.remove(transaction);
        } else {
            em.remove(em.merge(transaction));
        }
    }

    public List<Transaction> findBySourceAccountId(String accountId) {
        TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findBySourceAccountId", Transaction.class);
        query.setParameter("accountId", accountId);
        return query.getResultList();
    }
}
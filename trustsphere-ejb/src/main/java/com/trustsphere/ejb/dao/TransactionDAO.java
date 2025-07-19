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

    public List<Transaction> findByUser(String userId, int offset, int limit) {
        TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findByUserId", Transaction.class);
        query.setParameter("userId", userId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Transaction> findBySourceAccId(String accId, int offset, int limit) {
        TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findBySourceAccountId", Transaction.class);
        query.setParameter("accountId", accId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Transaction> findByTargetAccId(String accId, int offset, int limit) {
        TypedQuery<Transaction> query = em.createNamedQuery("Transaction.findBySourceAccountId", Transaction.class);
        query.setParameter("accountId", accId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
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

//    private TransactionDTO mapToDTO(Transaction transaction) {
//        TransactionDTO dto = new TransactionDTO();
//        dto.setId(transaction.getId());
//        dto.setAmount(transaction.getAmount());
//        dto.setSourceAccountId(transaction.getSourceAccount().getId());
//        dto.setTargetAccountId(transaction.getTargetAccount().getId());
//        dto.setType(transaction.getType());
//        dto.setTimestamp(transaction.getTimestamp());
//        dto.setStatus(transaction.getStatus());
//        return dto;
//    }
}
package com.trustsphere.ejb.dao;

import com.trustsphere.core.entity.Account;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class AccountDAO {

    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager em;

    public Account create(Account account) {
        em.persist(account);
        return account;
    }

    public Account findById(String id) {
        return em.find(Account.class, id);
    }

    public Account findByAccountNumber(String accountNumber) {
        TypedQuery<Account> query = em.createNamedQuery("Account.findByAccountNumber", Account.class);
        query.setParameter("accountNumber", accountNumber);
        List<Account> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Account> findByUserId(String userId) {
        TypedQuery<Account> query = em.createNamedQuery("Account.findByUserId", Account.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<Account> findActiveAccounts() {
        TypedQuery<Account> query = em.createQuery("SELECT a FROM Account a WHERE a.status = com.trustsphere.core.enums.AccountStatus.ACTIVE", Account.class);
        return query.getResultList();
    }


    public List<Account> findActiveByUserId(String userId) {
        TypedQuery<Account> query = em.createNamedQuery("Account.findActiveByUserId", Account.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public Account update(Account account) {
        return em.merge(account);
    }

    public void delete(Account account) {
        if (em.contains(account)) {
            em.remove(account);
        } else {
            em.remove(em.merge(account));
        }
    }

    public void flushBatch() {
        em.flush();
        em.clear();
    }
}
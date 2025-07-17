package com.trustsphere.ejb.dao;

import com.trustsphere.core.entity.User;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class UserDAO {

    @PersistenceContext(unitName = "trustspherePU")
    private EntityManager em;

    public User create(User user) {
        em.persist(user);
        return user;
    }

    public User findById(String id) {
        return em.find(User.class, id);
    }

    public User findByEmail(String email) {
        TypedQuery<User> query = em.createNamedQuery("User.findByEmail", User.class);
        query.setParameter("email", email);
        List<User> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<User> findAll() {
        TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
        return query.getResultList();
    }

    public User update(User user) {
        return em.merge(user);
    }

    public void delete(User user) {
        if (em.contains(user)) {
            em.remove(user);
        } else {
            em.remove(em.merge(user));
        }
    }
}
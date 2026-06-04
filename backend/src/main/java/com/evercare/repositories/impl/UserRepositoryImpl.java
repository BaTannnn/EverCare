/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.User;
import com.evercare.repositories.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author cadic
 */
@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {
    @Autowired
    private LocalSessionFactoryBean factory;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        return findByUsername(username);
    }

    @Override
    public User findByUsername(String username) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        root.fetch("roleSet", JoinType.LEFT);
        root.fetch("patient", JoinType.LEFT);
        root.fetch("employee", JoinType.LEFT);
        root.fetch("doctor", JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(cb.equal(root.get("username"), username));

        return session.createQuery(cq).uniqueResult();
    }

    @Override
    public User findById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        root.fetch("roleSet", JoinType.LEFT);
        root.fetch("patient", JoinType.LEFT);
        root.fetch("employee", JoinType.LEFT);
        root.fetch("doctor", JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).uniqueResult();
    }

    @Override
    public List<User> getActiveUsers() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        root.fetch("roleSet", JoinType.LEFT);
        root.fetch("employee", JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(cb.isTrue(root.get("active")));
        cq.orderBy(cb.asc(root.get("fullName")), cb.asc(root.get("username")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public boolean existsByUsername(String username) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("username"), username));

        Long count = session.createQuery(cq).uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        cq.select(cb.count(root));
        cq.where(cb.equal(cb.lower(root.get("email")), email.trim().toLowerCase()));

        Long count = session.createQuery(cq).uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        cq.select(cb.count(root));
        cq.where(cb.equal(root.get("phone"), phone));

        Long count = session.createQuery(cq).uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public User save(User u) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(u);
        session.flush();
        
        return u;
    }

    @Override
    public User update(User u) {
        Session session = this.factory.getObject().getCurrentSession();
        u = (User) session.merge(u);
        session.flush();
        return u;
    }

    @Override
    public boolean authenticate(String username, String password) {
        User u = this.findByUsername(username);
        if (u == null) {
            return false;
        }

        return this.passwordEncoder.matches(password, u.getPassword());
    }
}

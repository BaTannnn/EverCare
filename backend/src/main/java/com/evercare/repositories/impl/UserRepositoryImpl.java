/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.User;
import com.evercare.repositories.UserRepository;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        Query<User> q = session.createQuery(
            """
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN FETCH u.roleSet
            LEFT JOIN FETCH u.patient
            WHERE u.username = :username
            """,
                User.class
        );
        q.setParameter("username", username);

        return q.uniqueResult();
    }

    @Override
    public boolean existsByUsername(String username) {
        Session session = this.factory.getObject().getCurrentSession();
        Long count = session.createQuery(
                "SELECT COUNT(u.id) FROM User u WHERE u.username = :username",
                Long.class
        ).setParameter("username", username).uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        Session session = this.factory.getObject().getCurrentSession();
        Long count = session.createQuery(
                "SELECT COUNT(u.id) FROM User u WHERE LOWER(u.email) = LOWER(:email)",
                Long.class
        ).setParameter("email", email).uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }

        Session session = this.factory.getObject().getCurrentSession();
        Long count = session.createQuery(
                "SELECT COUNT(u.id) FROM User u WHERE u.phone = :phone",
                Long.class
        ).setParameter("phone", phone).uniqueResult();
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
    public boolean authenticate(String username, String password) {
        User u = this.findByUsername(username);
        if (u == null) {
            return false;
        }

        return this.passwordEncoder.matches(password, u.getPassword());
    }
}

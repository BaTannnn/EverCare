/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.UserRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    @Autowired
    private Environment env;

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

    private List<Predicate> buildPredicates(Map<String, String> params, CriteriaBuilder cb, Root<User> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            String kw = params.get("kw");
            if (kw != null && !kw.isBlank()) {
                String keyword = "%" + kw.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("fullName")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword)
                ));
            }

            String roleCode = params.get("roleCode");
            if (roleCode != null && !roleCode.isBlank()) {
                Join<User, Role> roleJoin = root.join("roleSet", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(roleJoin.get("code")), roleCode.trim().toLowerCase()));
            }

            String active = params.get("active");
            if (active != null && !active.isBlank()) {
                predicates.add(cb.equal(root.get("active"), Boolean.parseBoolean(active)));
            }
        }

        return predicates;
    }

    @Override
    public List<User> getUsers(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        root.fetch("roleSet", JoinType.LEFT);
        root.fetch("patient", JoinType.LEFT);
        root.fetch("employee", JoinType.LEFT);
        root.fetch("doctor", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(params, cb, root);

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("username")));

        org.hibernate.query.Query<User> query = session.createQuery(cq);

        if (params != null && params.containsKey("page") && !Boolean.parseBoolean(params.getOrDefault("noPaging", "false"))) {
            int pageSize = this.env.getProperty("account.pageSize", Integer.class);
            int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), this.countUsers(params), pageSize);
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public List<User> getActiveUsers() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        root.fetch("roleSet", JoinType.LEFT);
        root.fetch("patient", JoinType.LEFT);
        root.fetch("employee", JoinType.LEFT);
        root.fetch("doctor", JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(cb.isTrue(root.get("active")));
        cq.orderBy(cb.asc(root.get("fullName")), cb.asc(root.get("username")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public long countUsers(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<User> root = cq.from(User.class);

        cq.select(cb.countDistinct(root));
        cq.where(buildPredicates(params, cb, root).toArray(Predicate[]::new));

        Long count = session.createQuery(cq).uniqueResult();
        return count != null ? count : 0;
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        int pageSize = this.env.getProperty("account.pageSize", Integer.class);
        long count = this.countUsers(params);
        return (long) Math.max(1, Math.ceil((double) count / pageSize));
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

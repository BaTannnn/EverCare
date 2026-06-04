/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.Role;
import com.evercare.repositories.RoleRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cadic
 */
@Repository
@Transactional
public class RoleRepositoryImpl implements RoleRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Role findByCode(String code) {
        Session session = this.factory.getObject().getCurrentSession();
        Query<Role> q = session.createNamedQuery("Role.findByCode", Role.class);
        q.setParameter("code", code);

        return q.uniqueResult();
    }

    @Override
    public java.util.List<Role> getActiveRoles() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> root = cq.from(Role.class);

        cq.select(root);
        cq.where(cb.isTrue(root.get("active")));
        cq.orderBy(cb.asc(root.get("name")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public Role save(Role role) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(role);
        session.flush();
        return role;
    }
    
}

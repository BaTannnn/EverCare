/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.Role;
import com.evercare.repositories.RoleRepository;
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
    public Role getRoleByRoleName(String roleName) {
        Session session = this.factory.getObject().getCurrentSession();
        Query q = session.createNamedQuery("Role.findByCode", Role.class);
        q.setParameter("code", roleName);

        return (Role) q.getSingleResult();
    }
    
}

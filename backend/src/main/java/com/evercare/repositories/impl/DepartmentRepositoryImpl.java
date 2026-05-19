/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.Department;
import com.evercare.repositories.DepartmentRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class DepartmentRepositoryImpl implements DepartmentRepository {
    @Autowired
    private LocalSessionFactoryBean factory;
    
    @Override
    public List<Department> getDeparments(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Department> q = b.createQuery(Department.class);
        Root root = q.from(Department.class);
        q.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        if (params != null) {
        
            String kw = params.get("kw");
            if (kw != null && !kw.isEmpty()) {
                predicates.add(b.like(root.get("name"), String.format("%%%s%%", kw)));
            }
        }
        predicates.add(b.isTrue(root.get("active")));
        q.where(predicates.toArray(Predicate[]::new));
        q.orderBy(b.asc(root.get("name")));
        
        Query<Department> query = session.createQuery(q);
        
        return query.getResultList();
    }
    
    @Override
    public Department getDeparmentById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Department.class, id);    
    }

    @Override
    public void addOrUpdateDepartment(Department d) {
        Session s = this.factory.getObject().getCurrentSession();
        if (d.getId() == null) {
            s.persist(d);
        } else {
            s.merge(d);
        }
    }

    @Override
    public void sotfDelete(int id) {
        Session session = this.factory.getObject().getCurrentSession();

        Department department = session.get(Department.class, id);

        if (department != null) {
            department.setActive(false);
            session.merge(department);
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.evercare.repositories.impl;

import com.evercare.pojo.Department;
import com.evercare.repositories.DepartmentRepository;
import com.evercare.utils.PaginationUtils;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cadic
 */
@Repository
@PropertySource("classpath:configs.properties")
@Transactional
public class DepartmentRepositoryImpl implements DepartmentRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    private List<Predicate> getPredicate(Map<String, String> params, Root root, CriteriaBuilder b) {
        List<Predicate> predicates = new ArrayList<>();
        if (params != null) {

            String kw = params.get("kw");
            if (kw != null && !kw.isEmpty()) {
                predicates.add(b.like(root.get("name"), String.format("%%%s%%", kw)));
            }
        }
        predicates.add(b.isTrue(root.get("active")));
        return predicates;
    }
    
    @Override
    public List<Department> getDepartments(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Department> q = b.createQuery(Department.class);
        Root root = q.from(Department.class);
        q.select(root);
        
        List<Predicate> predicates = getPredicate(params, root, b);
        q.where(predicates.toArray(Predicate[]::new));
        q.orderBy(b.asc(root.get("name")));
        
        Query<Department> query = session.createQuery(q);

        if (params != null) {
            int page = PaginationUtils.getPage(params);
            int pageSize = this.env.getProperty("department.pageSize", Integer.class);
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }
        
        return query.getResultList();
    }
    
    @Override
    public Department getDepartmentById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Department.class, id);    
    }

    @Override
    public void addDepartment(Department department) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(department);
    }

    @Override
    public void updateDepartment(Department department) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(department);
    }

    @Override
    public void softDelete(int id) {
        Session session = this.factory.getObject().getCurrentSession();

        Department department = session.get(Department.class, id);

        if (department != null) {
            department.setActive(false);
            session.merge(department);
        }
    }

    @Override
    public long countDepartments(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root root = q.from(Department.class);
        q.select(b.count(root));

        List<Predicate> predicates = getPredicate(params, root, b);
        q.where(predicates.toArray(Predicate[]::new));

        Query<Long> query = session.createQuery(q);
        return query.getSingleResult();
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        long count = this.countDepartments(params);
        int pageSize = Integer.parseInt(env.getProperty("department.pageSize"));

        return (long) Math.ceil((double) count / pageSize);
    }
}

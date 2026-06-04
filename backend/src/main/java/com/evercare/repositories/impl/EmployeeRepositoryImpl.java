package com.evercare.repositories.impl;

import com.evercare.pojo.Employee;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class EmployeeRepositoryImpl implements EmployeeRepository {

    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    private List<Predicate> buildPredicates(Map<String, String> params, CriteriaBuilder cb, Root<Employee> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String kw = params.get("kw");
            if (kw != null && !kw.isBlank()) {
                String keyword = "%" + kw.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("employeeCode")), keyword),
                        cb.like(cb.lower(root.get("fullName")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("position")), keyword)
                ));
            }

            String position = params.get("position");
            if (position != null && !position.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("position")), position.trim().toLowerCase()));
            }
        }

        return predicates;
    }

    @Override
    public List<Employee> getEmployees(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);

        List<Predicate> predicates = buildPredicates(params, cb, root);

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.asc(root.get("fullName")));

        Query<Employee> query = session.createQuery(cq);

        if (params != null && params.containsKey("page") && !Boolean.parseBoolean(params.getOrDefault("noPaging", "false"))) {
            int pageSize = this.env.getProperty("employee.pageSize", Integer.class);
            int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), this.countEmployees(params), pageSize);
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public Employee getEmployeeById(int id) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);
        root.fetch("userId", JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(cb.equal(root.get("id"), Long.valueOf(id)));

        return session.createQuery(cq).uniqueResult();
    }

    @Override
    public Employee getEmployeeByUserId(Long userId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery(
                        "SELECT e FROM Employee e WHERE e.userId.id = :userId",
                        Employee.class
                )
                .setParameter("userId", userId)
                .uniqueResult();
    }

    @Override
    public void addEmployee(Employee employee) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(employee);
    }

    @Override
    public void updateEmployee(Employee employee) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(employee);
    }

    @Override
    public long countEmployees(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Employee> root = cq.from(Employee.class);

        cq.select(cb.countDistinct(root));
        cq.where(buildPredicates(params, cb, root).toArray(Predicate[]::new));

        Long count = session.createQuery(cq).uniqueResult();
        return count != null ? count : 0;
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        int pageSize = this.env.getProperty("employee.pageSize", Integer.class);
        long count = this.countEmployees(params);
        return (long) Math.max(1, Math.ceil((double) count / pageSize));
    }
}

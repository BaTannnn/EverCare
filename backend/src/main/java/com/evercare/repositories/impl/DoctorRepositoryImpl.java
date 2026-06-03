package com.evercare.repositories.impl;

import com.evercare.pojo.Doctor;
import com.evercare.repositories.DoctorRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.*;
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
public class DoctorRepositoryImpl implements DoctorRepository {
    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    private List<Predicate> getPredicates(Map<String, String> params, CriteriaBuilder cb, Root root) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String kw = params.get("kw");

            if (kw != null && !kw.isBlank()) {
                String keyword = "%" + kw.trim().toLowerCase() + "%";

                Predicate byName = cb.like(cb.lower(root.get("fullName")), keyword);
                Predicate byCode = cb.like(cb.lower(root.get("doctorCode")), keyword);
                Predicate byPhone = cb.like(cb.lower(root.get("phone")), keyword);
                Predicate byEmail = cb.like(cb.lower(root.get("email")), keyword);
                Predicate bySpecialization = cb.like(cb.lower(root.get("specialization")), keyword);

                predicates.add(cb.or(byName, byCode, byPhone, byEmail, bySpecialization));
            }

            String departmentId = params.get("departmentId");
            if (departmentId != null && !departmentId.isBlank()) {
                predicates.add(cb.equal(
                        root.get("departmentId").get("id"),
                        Long.parseLong(departmentId)
                ));
            }

            String doctorType = params.get("doctorType");
            if (doctorType != null && !doctorType.isBlank()) {
                predicates.add(cb.equal(root.get("doctorType"), doctorType));
            }

            String workStatus = params.get("workStatus");
            if (workStatus != null && !workStatus.isBlank()) {
                predicates.add(cb.equal(root.get("workStatus"), workStatus));
            }
        }
        return predicates;
    }

    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Doctor> cq = cb.createQuery(Doctor.class);
        Root<Doctor> root = cq.from(Doctor.class);
        root.fetch("departmentId", JoinType.LEFT);

        List<Predicate> predicates = getPredicates(params, cb, root);

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("fullName")));

        Query<Doctor> query = session.createQuery(cq);

        if (params != null && !params.isEmpty() && !Boolean.parseBoolean(params.getOrDefault("noPaging", "false"))) {
            int pageSize = this.env.getProperty("doctor.pageSize", Integer.class);
            int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), this.countDoctors(params), pageSize);
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public Doctor getDoctorById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Doctor.class, Long.valueOf(id));
    }
    @Override
    public Doctor getDoctorByUserId(Long userId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery(
                        "SELECT d FROM Doctor d WHERE d.userId.id = :userId",
                        Doctor.class
                )
                .setParameter("userId", userId)
                .uniqueResult();
    }

    @Override
    public void addDoctor(Doctor doctor) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(doctor);
    }

    @Override
    public void updateDoctor(Doctor doctor) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(doctor);
    }

    @Override
    public long countDoctors(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Doctor> root = cq.from(Doctor.class);
        cq.select(cb.count(root));

        List<Predicate> predicates = getPredicates(params, cb, root);

        cq.where(predicates.toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        long count = this.countDoctors(params);
        int pageSize = Integer.parseInt(env.getProperty("doctor.pageSize"));

        return (long) Math.max(1, Math.ceil((double) count / pageSize));
    }
}

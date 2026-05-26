package com.evercare.repositories.impl;

import com.evercare.pojo.Doctor;
import com.evercare.repositories.DoctorRepository;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LocalSessionFactoryBean factory;

    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Doctor> cq = cb.createQuery(Doctor.class);
        Root<Doctor> root = cq.from(Doctor.class);

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

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("fullName")));

        return session.createQuery(cq).getResultList();
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
}

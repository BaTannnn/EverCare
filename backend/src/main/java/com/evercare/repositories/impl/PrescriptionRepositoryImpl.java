package com.evercare.repositories.impl;

import com.evercare.enums.PrescriptionStatus;
import com.evercare.pojo.Prescription;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.utils.PaginationUtils;
import com.evercare.utils.QueryPagingSupport;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.*;

import java.time.LocalDate;
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

@Repository
@Transactional
public class PrescriptionRepositoryImpl implements PrescriptionRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    private List<Predicate> getPatientPredicates(Long patientId, String status, LocalDate from, LocalDate to, CriteriaBuilder cb, Root<Prescription> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("patientId").get("id"), patientId));

        if (status != null && !status.isBlank()) {
            predicates.add(cb.equal(root.get("status"), PrescriptionStatus.normalize(status)));
        }

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("prescribedAt"), java.sql.Timestamp.valueOf(from.atStartOfDay())));
        }

        if (to != null) {
            predicates.add(cb.lessThan(root.get("prescribedAt"), java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay())));
        }

        return predicates;
    }

    private void fetchPrescriptionGraph(Root<Prescription> root) {
        root.fetch("doctorId", JoinType.INNER);
        root.fetch("patientId", JoinType.INNER);
        Fetch<Prescription, ?> medicalRecordFetch = root.fetch("medicalRecordId", JoinType.INNER);
        medicalRecordFetch.fetch("appointmentId", JoinType.LEFT);
        medicalRecordFetch.fetch("invoice", JoinType.LEFT);
        Fetch<Prescription, ?> itemFetch = root.fetch("prescriptionItemSet", JoinType.LEFT);
        itemFetch.fetch("medicineId", JoinType.LEFT);
    }

    @Override
    public List<Prescription> getPrescriptions(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        String status = params != null ? params.get("status") : null;

        status = PrescriptionStatus.normalize(status);

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        fetchPrescriptionGraph(root);

        cq.select(root).distinct(true);
        cq.where(
                cb.isTrue(root.get("active")),
                cb.equal(root.get("status"), status)
        );
        cq.orderBy(cb.asc(root.get("prescribedAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        fetchPrescriptionGraph(root);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("doctorId").get("id"), doctorId));

        String status = params != null ? params.get("status") : null;
        if (status != null && !status.isBlank()) {
            status = PrescriptionStatus.normalize(status);
            predicates.add(cb.equal(root.get("status"), status));
        }

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("prescribedAt")), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<Prescription> getPrescriptionsByPatientId(Long patientId, String status, LocalDate from, LocalDate to, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        root.fetch("doctorId");
        root.fetch("patientId");
        root.fetch("medicalRecordId");

        cq.select(root);
        cq.where(getPatientPredicates(patientId, status, from, to, cb, root).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("prescribedAt")), cb.desc(root.get("id")));

        Query<Prescription> query = session.createQuery(cq);
        int pageSize = this.env.getProperty("patientRecord.pageSize", Integer.class);
        QueryPagingSupport.applyPaging(query, params, countPrescriptions(patientId, status, from, to), pageSize);

        return query.getResultList();
    }

    @Override
    public Prescription getPrescriptionById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        fetchPrescriptionGraph(root);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("id"), id),
                cb.isTrue(root.get("active"))
        );

        return session.createQuery(cq)
                .uniqueResult();
    }

    @Override
    public Prescription getPrescriptionByIdForUpdate(Long id) {
        Session session = this.factory.getObject().getCurrentSession();
        Prescription locked = session.find(Prescription.class, id, LockModeType.PESSIMISTIC_WRITE);

        if (locked == null || !Boolean.TRUE.equals(locked.getActive())) {
            return null;
        }

        return getPrescriptionById(id);
    }

    @Override
    public Prescription getPrescriptionByPatientIdAndId(Long patientId, Long id) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        fetchPrescriptionGraph(root);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("id"), id),
                cb.equal(root.get("patientId").get("id"), patientId),
                cb.isTrue(root.get("active"))
        );

        return session.createQuery(cq)
                .uniqueResult();
    }

    @Override
    public Prescription getPrescriptionByMedicalRecordId(Long medicalRecordId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        fetchPrescriptionGraph(root);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("medicalRecordId").get("id"), medicalRecordId),
                cb.isTrue(root.get("active"))
        );

        return session.createQuery(cq)
                .uniqueResult();
    }

    private long countPrescriptions(Long patientId, String status, LocalDate from, LocalDate to) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Prescription> root = cq.from(Prescription.class);

        cq.select(cb.countDistinct(root));
        cq.where(getPatientPredicates(patientId, status, from, to, cb, root).toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public void addPrescription(Prescription prescription) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(prescription);
        session.flush();
    }

    @Override
    public void updatePrescription(Prescription prescription) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(prescription);
    }
}

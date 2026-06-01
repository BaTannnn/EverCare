package com.evercare.repositories.impl;

import com.evercare.pojo.Prescription;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
@PropertySource("classpath:configs.properties")
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
            predicates.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
        }

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("prescribedAt"), java.sql.Timestamp.valueOf(from.atStartOfDay())));
        }

        if (to != null) {
            predicates.add(cb.lessThan(root.get("prescribedAt"), java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay())));
        }

        return predicates;
    }

    @Override
    public List<Prescription> getPrescriptions(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        String status = params != null ? params.get("status") : null;

        if (status == null || status.isBlank()) {
            status = "PRESCRIBED";
        }

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Prescription> cq = cb.createQuery(Prescription.class);
        Root<Prescription> root = cq.from(Prescription.class);

        root.fetch("doctorId");
        root.fetch("patientId");
        root.fetch("medicalRecordId");
        root.fetch("prescriptionItemSet", jakarta.persistence.criteria.JoinType.LEFT)
                .fetch("medicineId", jakarta.persistence.criteria.JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(
                cb.isTrue(root.get("active")),
                cb.equal(root.get("status"), status.trim().toUpperCase())
        );
        cq.orderBy(cb.asc(root.get("prescribedAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        StringBuilder hql = new StringBuilder("""
                SELECT DISTINCT p FROM Prescription p
                JOIN FETCH p.doctorId d
                JOIN FETCH p.patientId patient
                JOIN FETCH p.medicalRecordId mr
                LEFT JOIN FETCH mr.appointmentId appointment
                LEFT JOIN FETCH p.prescriptionItemSet item
                LEFT JOIN FETCH item.medicineId medicine
                WHERE p.active = true
                    AND d.id = :doctorId
                """);

        String status = params != null ? params.get("status") : null;
        if (status != null && !status.isBlank()) {
            hql.append(" AND p.status = :status");
        }

        hql.append(" ORDER BY p.prescribedAt DESC, p.id DESC");

        Query<Prescription> query = session.createQuery(hql.toString(), Prescription.class)
                .setParameter("doctorId", doctorId);

        if (status != null && !status.isBlank()) {
            query.setParameter("status", status.trim().toUpperCase());
        }

        return query.getResultList();
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
        root.fetch("prescriptionItemSet", jakarta.persistence.criteria.JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(getPatientPredicates(patientId, status, from, to, cb, root).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("prescribedAt")), cb.desc(root.get("id")));

        Query<Prescription> query = session.createQuery(cq);
        int pageSize = this.env.getProperty("patientRecord.pageSize", Integer.class);
        int normalizedPage = PaginationUtils.normalizePage(PaginationUtils.getPage(params), countPrescriptions(patientId, status, from, to), pageSize);
        query.setFirstResult((normalizedPage - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public Prescription getPrescriptionById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT p FROM Prescription p
                JOIN FETCH p.doctorId d
                JOIN FETCH p.patientId patient
                JOIN FETCH p.medicalRecordId mr
                LEFT JOIN FETCH p.prescriptionItemSet item
                LEFT JOIN FETCH item.medicineId medicine
                WHERE p.id = :id
                    AND p.active = true
                """, Prescription.class)
                .setParameter("id", id)
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

        return session.createQuery("""
                SELECT DISTINCT p FROM Prescription p
                JOIN FETCH p.doctorId d
                JOIN FETCH p.patientId patient
                JOIN FETCH p.medicalRecordId mr
                LEFT JOIN FETCH p.prescriptionItemSet item
                LEFT JOIN FETCH item.medicineId medicine
                WHERE p.id = :id
                    AND patient.id = :patientId
                    AND p.active = true
                """, Prescription.class)
                .setParameter("id", id)
                .setParameter("patientId", patientId)
                .uniqueResult();
    }

    @Override
    public Prescription getPrescriptionByMedicalRecordId(Long medicalRecordId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT p FROM Prescription p
                JOIN FETCH p.doctorId d
                JOIN FETCH p.patientId patient
                JOIN FETCH p.medicalRecordId mr
                LEFT JOIN FETCH p.prescriptionItemSet item
                LEFT JOIN FETCH item.medicineId medicine
                WHERE mr.id = :medicalRecordId
                    AND p.active = true
                """, Prescription.class)
                .setParameter("medicalRecordId", medicalRecordId)
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

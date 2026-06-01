package com.evercare.repositories.impl;

import com.evercare.pojo.MedicalRecord;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.utils.PaginationUtils;
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
@Transactional
public class MedicalRecordRepositoryImpl implements MedicalRecordRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    private List<Predicate> getPatientPredicates(Long patientId, LocalDate from, LocalDate to, CriteriaBuilder cb, Root<MedicalRecord> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("patientId").get("id"), patientId));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("visitDate"), java.sql.Timestamp.valueOf(from.atStartOfDay())));
        }

        if (to != null) {
            predicates.add(cb.lessThan(root.get("visitDate"), java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay())));
        }

        return predicates;
    }

    @Override
    public MedicalRecord getMedicalRecordById(Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT mr FROM MedicalRecord mr
                JOIN FETCH mr.doctorId d
                JOIN FETCH mr.patientId p
                JOIN FETCH mr.appointmentId a
                LEFT JOIN FETCH mr.invoice i
                LEFT JOIN FETCH a.medicalRecord amr
                WHERE mr.id = :recordId
                """, MedicalRecord.class)
                .setParameter("recordId", recordId)
                .uniqueResult();
    }

    @Override
    public List<MedicalRecord> getMedicalRecordsByPatientId(Long patientId, LocalDate from, LocalDate to, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicalRecord> cq = cb.createQuery(MedicalRecord.class);
        Root<MedicalRecord> root = cq.from(MedicalRecord.class);

        root.fetch("doctorId");
        root.fetch("patientId");
        root.fetch("appointmentId");
        root.fetch("prescription", jakarta.persistence.criteria.JoinType.LEFT);
        root.fetch("testResultSet", jakarta.persistence.criteria.JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(getPatientPredicates(patientId, from, to, cb, root).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("visitDate")), cb.desc(root.get("id")));

        Query<MedicalRecord> query = session.createQuery(cq);
        int pageSize = this.env.getProperty("patientRecord.pageSize", Integer.class);
        int normalizedPage = PaginationUtils.normalizePage(PaginationUtils.getPage(params), countMedicalRecords(patientId, from, to), pageSize);
        query.setFirstResult((normalizedPage - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public MedicalRecord getMedicalRecordByPatientIdAndId(Long patientId, Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT mr
                FROM MedicalRecord mr
                JOIN FETCH mr.doctorId d
                LEFT JOIN FETCH d.departmentId dept
                JOIN FETCH mr.patientId p
                LEFT JOIN FETCH mr.appointmentId a
                WHERE mr.active = true
                    AND p.id = :patientId
                    AND mr.id = :recordId
                """, MedicalRecord.class)
                .setParameter("patientId", patientId)
                .setParameter("recordId", recordId)
                .uniqueResult();
    }

    private long countMedicalRecords(Long patientId, LocalDate from, LocalDate to) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MedicalRecord> root = cq.from(MedicalRecord.class);

        cq.select(cb.countDistinct(root));
        cq.where(getPatientPredicates(patientId, from, to, cb, root).toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public void addMedicalRecord(MedicalRecord medicalRecord) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(medicalRecord);
        session.flush();
    }

    @Override
    public void updateMedicalRecord(MedicalRecord medicalRecord) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(medicalRecord);
    }
}

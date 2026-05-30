package com.evercare.repositories.impl;

import com.evercare.pojo.TestResult;
import com.evercare.repositories.TestResultRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
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
public class TestResultRepositoryImpl implements TestResultRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    private List<Predicate> getPatientPredicates(Long patientId, LocalDate from, LocalDate to, CriteriaBuilder cb, Root<TestResult> root) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("medicalRecordId").get("patientId").get("id"), patientId));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("resultDate"), java.sql.Timestamp.valueOf(from.atStartOfDay())));
        }

        if (to != null) {
            predicates.add(cb.lessThan(root.get("resultDate"), java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay())));
        }

        return predicates;
    }

    @Override
    public void addTestResult(TestResult testResult) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(testResult);
        session.flush();
    }

    @Override
    public TestResult getTestResultById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT tr FROM TestResult tr
                JOIN FETCH tr.medicalRecordId mr
                LEFT JOIN FETCH tr.serviceId s
                LEFT JOIN FETCH tr.performedBy e
                WHERE tr.id = :id
                    AND tr.active = true
                """, TestResult.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public void updateTestResult(TestResult testResult) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(testResult);
    }

    @Override
    public boolean existsByMedicalRecordIdAndServiceId(Long recordId, Long serviceId, Long excludeId) {
        Session session = this.factory.getObject().getCurrentSession();

        String hql = """
                SELECT COUNT(tr.id)
                FROM TestResult tr
                WHERE tr.active = true
                    AND tr.medicalRecordId.id = :recordId
                    AND tr.serviceId.id = :serviceId
                """;

        if (excludeId != null) {
            hql += " AND tr.id <> :excludeId";
        }

        Query<Long> query = session.createQuery(hql, Long.class)
                .setParameter("recordId", recordId)
                .setParameter("serviceId", serviceId);

        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        Long count = query.uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public List<TestResult> getTestResults(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TestResult> cq = cb.createQuery(TestResult.class);
        Root<TestResult> root = cq.from(TestResult.class);

        root.fetch("medicalRecordId");
        root.fetch("serviceId", JoinType.LEFT);
        root.fetch("performedBy", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String recordId = params.get("recordId");
            if (recordId != null && !recordId.isBlank()) {
                predicates.add(cb.equal(root.get("medicalRecordId").get("id"), Long.parseLong(recordId)));
            }

            String serviceId = params.get("serviceId");
            if (serviceId != null && !serviceId.isBlank()) {
                predicates.add(cb.equal(root.get("serviceId").get("id"), Long.parseLong(serviceId)));
            }

            String fromDate = params.get("fromDate");
            if (fromDate != null && !fromDate.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("resultDate"), java.sql.Timestamp.valueOf(LocalDate.parse(fromDate).atStartOfDay())));
            }

            String toDate = params.get("toDate");
            if (toDate != null && !toDate.isBlank()) {
                predicates.add(cb.lessThan(root.get("resultDate"), java.sql.Timestamp.valueOf(LocalDate.parse(toDate).plusDays(1).atStartOfDay())));
            }
        }

        cq.select(root).distinct(true);
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("resultDate")), cb.desc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<TestResult> getTestResultsByMedicalRecordId(Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT tr FROM TestResult tr
                JOIN FETCH tr.medicalRecordId mr
                LEFT JOIN FETCH tr.serviceId s
                LEFT JOIN FETCH tr.performedBy e
                WHERE mr.id = :recordId
                    AND tr.active = true
                ORDER BY tr.resultDate ASC, tr.id ASC
                """, TestResult.class)
                .setParameter("recordId", recordId)
                .getResultList();
    }

    @Override
    public List<TestResult> getTestResultsByPatientId(Long patientId, LocalDate from, LocalDate to, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<TestResult> cq = cb.createQuery(TestResult.class);
        Root<TestResult> root = cq.from(TestResult.class);

        root.fetch("medicalRecordId");
        root.fetch("serviceId", jakarta.persistence.criteria.JoinType.LEFT);
        root.fetch("performedBy", jakarta.persistence.criteria.JoinType.LEFT);

        cq.select(root).distinct(true);
        cq.where(getPatientPredicates(patientId, from, to, cb, root).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("resultDate")), cb.desc(root.get("id")));

        Query<TestResult> query = session.createQuery(cq);
        int pageSize = this.env.getProperty("patientRecord.pageSize", Integer.class);
        int normalizedPage = PaginationUtils.normalizePage(PaginationUtils.getPage(params), countTestResults(patientId, from, to), pageSize);
        query.setFirstResult((normalizedPage - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    private long countTestResults(Long patientId, LocalDate from, LocalDate to) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<TestResult> root = cq.from(TestResult.class);

        cq.select(cb.countDistinct(root));
        cq.where(getPatientPredicates(patientId, from, to, cb, root).toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }
}

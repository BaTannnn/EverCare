package com.evercare.repositories.impl;

import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.TestResult;
import com.evercare.repositories.MedicalRecordServiceRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MedicalRecordServiceRepositoryImpl implements MedicalRecordServiceRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public void addMedicalRecordService(MedicalRecordService medicalRecordService) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(medicalRecordService);
        session.flush();
    }

    @Override
    public List<MedicalRecordService> getServicesByMedicalRecordId(Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicalRecordService> cq = cb.createQuery(MedicalRecordService.class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);

        root.fetch("medicalRecordId", JoinType.INNER);
        root.fetch("serviceId", JoinType.INNER);

        cq.select(root).distinct(true);
        cq.where(
                cb.equal(root.get("medicalRecordId").get("id"), recordId),
                cb.isTrue(root.get("active"))
        );
        cq.orderBy(cb.asc(root.get("createdAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public List<MedicalRecordService> getPendingTestRequests() {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MedicalRecordService> cq = cb.createQuery(MedicalRecordService.class);
        Root<MedicalRecordService> root = cq.from(MedicalRecordService.class);

        Fetch<MedicalRecordService, ?> medicalRecordFetch = root.fetch("medicalRecordId", JoinType.INNER);
        medicalRecordFetch.fetch("patientId", JoinType.INNER);
        medicalRecordFetch.fetch("doctorId", JoinType.INNER);
        root.fetch("serviceId", JoinType.INNER);

        Subquery<Long> resultSubquery = cq.subquery(Long.class);
        Root<TestResult> testResult = resultSubquery.from(TestResult.class);
        resultSubquery.select(testResult.get("id"));
        resultSubquery.where(
                cb.isTrue(testResult.get("active")),
                cb.equal(testResult.get("medicalRecordId").get("id"), root.get("medicalRecordId").get("id")),
                cb.equal(testResult.get("serviceId").get("id"), root.get("serviceId").get("id"))
        );

        Predicate activeRequest = cb.isTrue(root.get("active"));
        Predicate activeRecord = cb.isTrue(root.get("medicalRecordId").get("active"));
        Predicate hasNoActiveResult = cb.not(cb.exists(resultSubquery));

        cq.select(root).distinct(true);
        cq.where(activeRequest, activeRecord, hasNoActiveResult);
        cq.orderBy(cb.asc(root.get("createdAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }
}

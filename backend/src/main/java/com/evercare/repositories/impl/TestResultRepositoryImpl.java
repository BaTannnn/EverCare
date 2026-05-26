package com.evercare.repositories.impl;

import com.evercare.pojo.TestResult;
import com.evercare.repositories.TestResultRepository;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TestResultRepositoryImpl implements TestResultRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

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
}

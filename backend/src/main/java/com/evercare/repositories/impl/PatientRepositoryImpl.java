package com.evercare.repositories.impl;

import com.evercare.pojo.Patient;
import com.evercare.repositories.PatientRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PatientRepositoryImpl implements PatientRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Patient getPatientById(Long id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.createQuery("""
                SELECT p
                FROM Patient p
                LEFT JOIN FETCH p.userId u
                WHERE p.id = :id
                """, Patient.class)
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Patient getPatientByUserId(Long userId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.createQuery("""
                SELECT p
                FROM Patient p
                JOIN FETCH p.userId u
                WHERE u.id = :userId
                """, Patient.class)
                .setParameter("userId", userId)
                .uniqueResult();
    }

    @Override
    public Patient getPatientByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        Session session = this.factory.getObject().getCurrentSession();
        return session.createQuery("""
                SELECT p
                FROM Patient p
                LEFT JOIN FETCH p.userId u
                WHERE p.phone = :phone
                """, Patient.class)
                .setParameter("phone", phone.trim())
                .uniqueResult();
    }

    @Override
    public Patient getPatientByCitizenId(String citizenId) {
        if (citizenId == null || citizenId.isBlank()) {
            return null;
        }

        Session session = this.factory.getObject().getCurrentSession();
        return session.createQuery("""
                SELECT p
                FROM Patient p
                LEFT JOIN FETCH p.userId u
                WHERE p.citizenId = :citizenId
                """, Patient.class)
                .setParameter("citizenId", citizenId.trim())
                .uniqueResult();
    }

    @Override
    public boolean existsActiveByUserId(Long userId) {
        Session session = this.factory.getObject().getCurrentSession();
        Long count = session.createQuery("""
                SELECT COUNT(p.id)
                FROM Patient p
                WHERE p.userId.id = :userId
                    AND p.active = true
                """, Long.class)
                .setParameter("userId", userId)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCitizenId(String citizenId) {
        if (citizenId == null || citizenId.isBlank()) {
            return false;
        }
        Session session = this.factory.getObject().getCurrentSession();
        Long count = session.createQuery("""
                SELECT COUNT(p.id)
                FROM Patient p
                WHERE p.citizenId = :citizenId
                """, Long.class)
                .setParameter("citizenId", citizenId)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByHealthInsuranceNo(String healthInsuranceNo) {
        if (healthInsuranceNo == null || healthInsuranceNo.isBlank()) {
            return false;
        }
        Session session = this.factory.getObject().getCurrentSession();
        Long count = session.createQuery("""
                SELECT COUNT(p.id)
                FROM Patient p
                WHERE p.healthInsuranceNo = :healthInsuranceNo
                """, Long.class)
                .setParameter("healthInsuranceNo", healthInsuranceNo)
                .uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Patient save(Patient patient) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(patient);
        session.flush();
        return patient;
    }

    @Override
    public Patient update(Patient patient) {
        Session session = this.factory.getObject().getCurrentSession();
        patient = (Patient) session.merge(patient);
        session.flush();
        return patient;
    }
}

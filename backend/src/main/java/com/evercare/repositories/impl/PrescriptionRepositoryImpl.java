package com.evercare.repositories.impl;

import com.evercare.pojo.Prescription;
import com.evercare.repositories.PrescriptionRepository;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PrescriptionRepositoryImpl implements PrescriptionRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Prescription> getPrescriptions(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        String status = params != null ? params.get("status") : null;

        if (status == null || status.isBlank()) {
            status = "PRESCRIBED";
        }

        return session.createQuery("""
                SELECT p FROM Prescription p
                JOIN FETCH p.doctorId d
                JOIN FETCH p.patientId patient
                JOIN FETCH p.medicalRecordId mr
                WHERE p.active = true
                    AND p.status = :status
                ORDER BY p.prescribedAt ASC, p.id ASC
                """, Prescription.class)
                .setParameter("status", status.trim().toUpperCase())
                .getResultList();
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
}

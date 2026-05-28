package com.evercare.repositories.impl;

import com.evercare.pojo.MedicalRecordService;
import com.evercare.repositories.MedicalRecordServiceRepository;
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

        return session.createQuery("""
                SELECT mrs FROM MedicalRecordService mrs
                JOIN FETCH mrs.medicalRecordId mr
                JOIN FETCH mrs.serviceId s
                WHERE mr.id = :recordId
                    AND mrs.active = true
                ORDER BY mrs.createdAt ASC, mrs.id ASC
                """, MedicalRecordService.class)
                .setParameter("recordId", recordId)
                .getResultList();
    }

    @Override
    public List<MedicalRecordService> getPendingTestRequests() {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT mrs FROM MedicalRecordService mrs
                JOIN FETCH mrs.medicalRecordId mr
                JOIN FETCH mr.patientId patient
                JOIN FETCH mr.doctorId doctor
                JOIN FETCH mrs.serviceId service
                WHERE mrs.active = true
                    AND mr.active = true
                    AND NOT EXISTS (
                        SELECT tr.id FROM TestResult tr
                        WHERE tr.active = true
                            AND tr.medicalRecordId.id = mr.id
                            AND tr.serviceId.id = service.id
                    )
                ORDER BY mrs.createdAt ASC, mrs.id ASC
                """, MedicalRecordService.class)
                .getResultList();
    }
}

package com.evercare.repositories.impl;

import com.evercare.pojo.MedicalRecord;
import com.evercare.repositories.MedicalRecordRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MedicalRecordRepositoryImpl implements MedicalRecordRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public MedicalRecord getMedicalRecordById(Long recordId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT mr FROM MedicalRecord mr
                JOIN FETCH mr.doctorId d
                JOIN FETCH mr.patientId p
                JOIN FETCH mr.appointmentId a
                LEFT JOIN FETCH a.medicalRecord amr
                WHERE mr.id = :recordId
                """, MedicalRecord.class)
                .setParameter("recordId", recordId)
                .uniqueResult();
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

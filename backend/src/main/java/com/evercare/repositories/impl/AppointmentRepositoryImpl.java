package com.evercare.repositories.impl;

import com.evercare.pojo.Appointment;
import com.evercare.repositories.AppointmentRepository;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class AppointmentRepositoryImpl implements AppointmentRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT a FROM Appointment a
                JOIN FETCH a.patientId p
                WHERE a.doctorId.id = :doctorId
                    AND a.appointmentDate = :appointmentDate
                    AND a.active = true
                ORDER BY a.startTime ASC, a.id ASC
                """, Appointment.class)
                .setParameter("doctorId", doctorId)
                .setParameter("appointmentDate", appointmentDate)
                .getResultList();
    }

    @Override
    public Map<String, Long> countAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate) {
        Session session = this.factory.getObject().getCurrentSession();
        List<Object[]> rows = session.createQuery("""
                SELECT a.status, COUNT(a)
                FROM Appointment a
                WHERE a.doctorId.id = :doctorId
                    AND a.appointmentDate = :appointmentDate
                    AND a.active = true
                GROUP BY a.status
                """, Object[].class)
                .setParameter("doctorId", doctorId)
                .setParameter("appointmentDate", appointmentDate)
                .getResultList();

        Map<String, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            String status = (String) row[0];
            Number count = (Number) row[1];
            counts.put(status, count.longValue());
        }

        return counts;
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT a FROM Appointment a
                JOIN FETCH a.doctorId d
                JOIN FETCH a.patientId p
                LEFT JOIN FETCH a.serviceId s
                LEFT JOIN FETCH a.medicalRecord mr
                LEFT JOIN FETCH mr.prescription pr
                LEFT JOIN FETCH pr.prescriptionItemSet item
                LEFT JOIN FETCH item.medicineId medicine
                WHERE a.id = :appointmentId
                    AND a.active = true
                """, Appointment.class)
                .setParameter("appointmentId", appointmentId)
                .uniqueResult();
    }

    @Override
    public Appointment getAppointmentByDoctorAndId(Long doctorId, Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT DISTINCT a FROM Appointment a
                JOIN FETCH a.patientId p
                LEFT JOIN FETCH a.serviceId s
                LEFT JOIN FETCH a.medicalRecord mr
                LEFT JOIN FETCH mr.prescription pr
                LEFT JOIN FETCH pr.prescriptionItemSet item
                LEFT JOIN FETCH item.medicineId medicine
                WHERE a.doctorId.id = :doctorId
                    AND a.id = :appointmentId
                    AND a.active = true
                """, Appointment.class)
                .setParameter("doctorId", doctorId)
                .setParameter("appointmentId", appointmentId)
                .uniqueResult();
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(appointment);
    }
}

package com.evercare.repositories.impl;

import com.evercare.pojo.Appointment;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.utils.PaginationUtils;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.sql.Time;
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
public class AppointmentRepositoryImpl implements AppointmentRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    @Override
    public List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate) {
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
                SELECT a.status, COUNT(a.id)
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
            counts.put((String) row[0], (Long) row[1]);
        }

        return counts;
    }

    @Override
    public List<Appointment> getAppointmentsByPatientId(Long patientId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        StringBuilder hql = new StringBuilder("""
                SELECT DISTINCT a
                FROM Appointment a
                JOIN FETCH a.doctorId d
                LEFT JOIN FETCH d.departmentId dept
                LEFT JOIN FETCH a.serviceId s
                WHERE a.active = true
                  AND a.patientId.id = :patientId
                """);

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isBlank()) {
                hql.append(" AND a.status = :status");
            }

            String from = params.get("from");
            if (from != null && !from.isBlank()) {
                hql.append(" AND a.appointmentDate >= :fromDate");
            }

            String to = params.get("to");
            if (to != null && !to.isBlank()) {
                hql.append(" AND a.appointmentDate <= :toDate");
            }
        }

        hql.append(" ORDER BY a.appointmentDate DESC, a.startTime ASC, a.id DESC");

        Query<Appointment> query = session.createQuery(hql.toString(), Appointment.class)
                .setParameter("patientId", patientId);

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isBlank()) {
                query.setParameter("status", status.trim().toUpperCase());
            }

            String from = params.get("from");
            if (from != null && !from.isBlank()) {
                query.setParameter("fromDate", java.sql.Date.valueOf(java.time.LocalDate.parse(from)));
            }

            String to = params.get("to");
            if (to != null && !to.isBlank()) {
                query.setParameter("toDate", java.sql.Date.valueOf(java.time.LocalDate.parse(to)));
            }

            int pageSize = this.env.getProperty("appointment.pageSize", Integer.class);
            int page = PaginationUtils.normalizePage(
                    PaginationUtils.getPage(params),
                    countAppointmentsByPatient(patientId, params),
                    pageSize
            );
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
        }

        return query.getResultList();
    }

    @Override
    public Appointment getAppointmentByPatientIdAndId(Long patientId, Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT a FROM Appointment a
                JOIN FETCH a.doctorId d
                LEFT JOIN FETCH d.departmentId dept
                LEFT JOIN FETCH a.serviceId s
                JOIN FETCH a.patientId p
                WHERE a.id = :appointmentId
                    AND p.id = :patientId
                    AND a.active = true
                """, Appointment.class)
                .setParameter("appointmentId", appointmentId)
                .setParameter("patientId", patientId)
                .uniqueResult();
    }

    @Override
    public boolean existsAppointmentByDoctorAndTime(Long doctorId, Date appointmentDate, Time startTime, Long excludeId) {
        Session session = this.factory.getObject().getCurrentSession();

        String hql = """
                SELECT COUNT(a.id)
                FROM Appointment a
                WHERE a.doctorId.id = :doctorId
                    AND a.appointmentDate = :appointmentDate
                    AND a.startTime = :startTime
                    AND a.active = true
                    AND (a.status IS NULL OR (a.status <> 'CANCELLED' AND a.status <> 'NO_SHOW'))
                """;

        if (excludeId != null) {
            hql += " AND a.id <> :excludeId";
        }

        var query = session.createQuery(hql, Long.class);
        query.setParameter("doctorId", doctorId);
        query.setParameter("appointmentDate", appointmentDate);
        query.setParameter("startTime", startTime);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        Long count = query.uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(appointment);
        session.flush();
        return appointment;
    }

    @Override
    public long countBookedAppointmentsByDoctorAndDateAndWindow(Long doctorId, Date appointmentDate, Time startTime, Time endTime) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT COUNT(a.id)
                FROM Appointment a
                WHERE a.doctorId.id = :doctorId
                    AND a.appointmentDate = :appointmentDate
                    AND a.active = true
                    AND (a.status IS NULL OR (a.status <> 'CANCELLED' AND a.status <> 'NO_SHOW'))
                    AND a.startTime >= :startTime
                    AND a.startTime < :endTime
                """, Long.class)
                .setParameter("doctorId", doctorId)
                .setParameter("appointmentDate", appointmentDate)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .uniqueResult();
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT a FROM Appointment a
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
                SELECT a FROM Appointment a
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

    private long countAppointmentsByPatient(Long patientId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        StringBuilder hql = new StringBuilder("""
                SELECT COUNT(DISTINCT a.id)
                FROM Appointment a
                WHERE a.active = true
                  AND a.patientId.id = :patientId
                """);

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isBlank()) {
                hql.append(" AND a.status = :status");
            }

            String from = params.get("from");
            if (from != null && !from.isBlank()) {
                hql.append(" AND a.appointmentDate >= :fromDate");
            }

            String to = params.get("to");
            if (to != null && !to.isBlank()) {
                hql.append(" AND a.appointmentDate <= :toDate");
            }
        }

        Query<Long> query = session.createQuery(hql.toString(), Long.class)
                .setParameter("patientId", patientId);

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isBlank()) {
                query.setParameter("status", status.trim().toUpperCase());
            }

            String from = params.get("from");
            if (from != null && !from.isBlank()) {
                query.setParameter("fromDate", java.sql.Date.valueOf(java.time.LocalDate.parse(from)));
            }

            String to = params.get("to");
            if (to != null && !to.isBlank()) {
                query.setParameter("toDate", java.sql.Date.valueOf(java.time.LocalDate.parse(to)));
            }
        }

        Long count = query.uniqueResult();
        return count != null ? count : 0L;
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(appointment);
    }
}

package com.evercare.repositories.impl;

import com.evercare.pojo.Appointment;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.utils.PaginationUtils;
import com.evercare.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.sql.Time;
import java.util.ArrayList;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.evercare.pojo.Department;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.Patient;
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
public class AppointmentRepositoryImpl implements AppointmentRepository {
    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    @Override
    public List<Appointment> getAppointmentsForReceptionist(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        Fetch<Appointment, Doctor> doctorFetch = root.fetch("doctorId", JoinType.INNER);
        doctorFetch.fetch("departmentId", JoinType.LEFT);
        root.fetch("serviceId", JoinType.LEFT);
        Join<Appointment, Doctor> doctorJoin = root.join("doctorId", JoinType.INNER);

        List<Predicate> predicates = buildReceptionistPredicates(params, builder, root, patientJoin, doctorJoin);

        query.select(root).distinct(true);
        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(
                builder.asc(root.get("appointmentDate")),
                builder.asc(root.get("startTime")),
                builder.asc(root.get("id"))
        );

        Query<Appointment> hQuery = session.createQuery(query);

        if (params != null) {
            int pageSize = resolvePageSize(params);
            int page = PaginationUtils.normalizePage(
                    PaginationUtils.getPage(params),
                    countAppointmentsForReceptionist(params),
                    pageSize
            );
            hQuery.setFirstResult((page - 1) * pageSize);
            hQuery.setMaxResults(pageSize);
        }

        return hQuery.getResultList();
    }

    @Override
    public long countAppointmentsForReceptionist(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Appointment> root = query.from(Appointment.class);
        Join<Appointment, Patient> patientJoin = root.join("patientId", JoinType.INNER);
        Join<Appointment, Doctor> doctorJoin = root.join("doctorId", JoinType.INNER);
        doctorJoin.join("departmentId", JoinType.LEFT);
        root.join("serviceId", JoinType.LEFT);

        List<Predicate> predicates = buildReceptionistPredicates(params, builder, root, patientJoin, doctorJoin);
        query.select(builder.countDistinct(root));
        query.where(predicates.toArray(Predicate[]::new));
        return session.createQuery(query).getSingleResult();
    }

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
        return countBookedAppointmentsByDoctorAndDateAndWindow(doctorId, appointmentDate, startTime, endTime, null);
    }

    @Override
    public long countBookedAppointmentsByDoctorAndDateAndWindow(Long doctorId, Date appointmentDate, Time startTime, Time endTime, Long excludeId) {
        Session session = this.factory.getObject().getCurrentSession();

        String hql = """
                SELECT COUNT(a.id)
                FROM Appointment a
                WHERE a.doctorId.id = :doctorId
                    AND a.appointmentDate = :appointmentDate
                    AND a.active = true
                    AND (a.status IS NULL OR (a.status <> 'CANCELLED' AND a.status <> 'NO_SHOW'))
                    AND a.startTime >= :startTime
                    AND a.startTime < :endTime
                """;

        if (excludeId != null) {
            hql += " AND a.id <> :excludeId";
        }

        Query<Long> query = session.createQuery(hql, Long.class)
                .setParameter("doctorId", doctorId)
                .setParameter("appointmentDate", appointmentDate)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        return query.uniqueResult();
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

    private List<Predicate> buildReceptionistPredicates(Map<String, String> params,
                                                        CriteriaBuilder builder,
                                                        Root<Appointment> root,
                                                        Join<Appointment, Patient> patientJoin,
                                                        Join<Appointment, Doctor> doctorJoin) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.isTrue(root.get("active")));

        LocalDate appointmentDate = LocalDate.now();
        String dateValue = params != null ? params.get("date") : null;
        if (dateValue != null && !dateValue.isBlank()) {
            appointmentDate = LocalDate.parse(dateValue.trim());
        }
        predicates.add(builder.equal(root.get("appointmentDate"), java.sql.Date.valueOf(appointmentDate)));

        String statusValue = params != null ? params.get("status") : null;
        String status = (statusValue == null || statusValue.isBlank())
                ? AppointmentStatus.BOOKED.getCode()
                : AppointmentStatus.normalize(statusValue);
        predicates.add(builder.equal(root.get("status"), status));

        if (params != null) {
            String doctorId = params.get("doctorId");
            if (doctorId != null && !doctorId.isBlank()) {
                predicates.add(builder.equal(doctorJoin.get("id"), Long.parseLong(doctorId.trim())));
            }

            String departmentId = params.get("departmentId");
            if (departmentId != null && !departmentId.isBlank()) {
                predicates.add(builder.equal(doctorJoin.get("departmentId").get("id"), Long.parseLong(departmentId.trim())));
            }

            String keyword = params.get("keyword");
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";

                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("appointmentCode")), like),
                        builder.like(builder.lower(patientJoin.get("phone")), like),
                        builder.like(builder.lower(patientJoin.get("fullName")), like),
                        builder.like(builder.lower(patientJoin.get("citizenId")), like)
                ));
            }
        }

        return predicates;
    }

    private int resolvePageSize(Map<String, String> params) {
        int defaultPageSize = this.env.getProperty("appointment.pageSize", Integer.class);
        if (params == null) {
            return defaultPageSize;
        }

        String sizeValue = params.get("size");
        if (sizeValue == null || sizeValue.isBlank()) {
            return defaultPageSize;
        }

        try {
            int size = Integer.parseInt(sizeValue.trim());
            return size > 0 ? size : defaultPageSize;
        } catch (NumberFormatException ex) {
            return defaultPageSize;
        }
    }
}

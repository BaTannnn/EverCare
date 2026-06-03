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
import com.evercare.utils.QueryPagingSupport;
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
        Join<Appointment, Doctor> doctorJoin = root.join("doctorId", JoinType.INNER);

        Fetch<Appointment, Doctor> doctorFetch = root.fetch("doctorId", JoinType.INNER);
        doctorFetch.fetch("departmentId", JoinType.LEFT);
        root.fetch("serviceId", JoinType.LEFT);
        root.fetch("patientId", JoinType.INNER);
        root.fetch("medicalRecord", JoinType.LEFT);

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
            int pageSize = QueryPagingSupport.resolvePageSize(this.env, "appointment.pageSize", params, 10);
            QueryPagingSupport.applyPaging(hQuery, params, countAppointmentsForReceptionist(params), pageSize);
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

        List<Predicate> predicates = buildReceptionistPredicates(params, builder, root, patientJoin, doctorJoin);
        query.select(builder.count(root));
        query.where(predicates.toArray(Predicate[]::new));
        return session.createQuery(query).getSingleResult();
    }

    @Override
    public List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);

        root.fetch("patientId", JoinType.INNER);
        root.fetch("serviceId", JoinType.LEFT);
        Fetch<Appointment, ?> medicalRecordFetch = root.fetch("medicalRecord", JoinType.LEFT);
        medicalRecordFetch.fetch("invoice", JoinType.LEFT);
        medicalRecordFetch.fetch("prescription", JoinType.LEFT);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("doctorId").get("id"), doctorId),
                builder.equal(root.get("appointmentDate"), appointmentDate),
                builder.isTrue(root.get("active"))
        );
        query.orderBy(builder.asc(root.get("startTime")), builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Map<String, Long> countAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
        Root<Appointment> root = query.from(Appointment.class);

        query.multiselect(
                root.get("status"),
                builder.count(root.get("id"))
        );

        query.where(
                builder.equal(root.get("doctorId").get("id"), doctorId),
                builder.equal(root.get("appointmentDate"), appointmentDate),
                builder.isTrue(root.get("active"))
        );

        query.groupBy(root.get("status"));

        List<Object[]> rows = session.createQuery(query).getResultList();

        Map<String, Long> counts = new HashMap<>();

        for (Object[] row : rows) {
            String status = (String) row[0];
            Long count = (Long) row[1];

            counts.put(status, count);
        }

        return counts;
    }

    @Override
    public List<Appointment> getAppointmentsByPatientId(Long patientId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);
        Fetch<Appointment, Doctor> doctorFetch = root.fetch("doctorId", JoinType.INNER);
        doctorFetch.fetch("departmentId", JoinType.LEFT);
        root.fetch("serviceId", JoinType.LEFT);
        root.fetch("patientId", JoinType.INNER);
        root.fetch("medicalRecord", JoinType.LEFT);

        List<Predicate> predicates = buildPatientPredicates(patientId, builder, root, params);

        query.select(root).distinct(true);
        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(
                builder.desc(root.get("appointmentDate")),
                builder.asc(root.get("startTime")),
                builder.desc(root.get("id"))
        );

        Query<Appointment> hQuery = session.createQuery(query);
        if (params != null) {
            int pageSize = QueryPagingSupport.resolvePageSize(this.env, "appointment.pageSize", params, 10);
            QueryPagingSupport.applyPaging(hQuery, params, countAppointmentsByPatient(patientId, params), pageSize);
        }

        return hQuery.getResultList();
    }

    private long countAppointmentsByPatient(Long patientId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Appointment> root = query.from(Appointment.class);

        List<Predicate> predicates = buildPatientPredicates(patientId, builder, root, params);

        query.select(builder.count(root.get("id")));
        query.where(predicates.toArray(Predicate[]::new));

        Query<Long> hQuery = session.createQuery(query);
        return hQuery.getSingleResult();
    }

    @Override
    public Appointment getAppointmentByPatientIdAndId(Long patientId, Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);
        Fetch<Appointment, Doctor> doctorFetch = root.fetch("doctorId", JoinType.INNER);
        doctorFetch.fetch("departmentId", JoinType.LEFT);
        root.fetch("serviceId", JoinType.LEFT);
        root.fetch("patientId", JoinType.INNER);
        root.fetch("medicalRecord", JoinType.LEFT);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("id"), appointmentId),
                builder.equal(root.get("patientId").get("id"), patientId),
                builder.isTrue(root.get("active"))
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Appointment getAppointmentByPatientDoctorAndSlot(Long patientId, Long doctorId, Date appointmentDate, Time startTime, Time endTime) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);
        Fetch<Appointment, Doctor> doctorFetch = root.fetch("doctorId", JoinType.INNER);
        doctorFetch.fetch("departmentId", JoinType.LEFT);
        root.fetch("patientId", JoinType.INNER);
        root.fetch("serviceId", JoinType.LEFT);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("patientId").get("id"), patientId),
                builder.equal(root.get("doctorId").get("id"), doctorId),
                builder.equal(root.get("appointmentDate"), appointmentDate),
                builder.equal(root.get("startTime"), startTime),
                builder.equal(root.get("endTime"), endTime),
                builder.isTrue(root.get("active"))
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
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

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Appointment> root = query.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get("doctorId").get("id"), doctorId));
        predicates.add(builder.equal(root.get("appointmentDate"), appointmentDate));
        predicates.add(builder.isTrue(root.get("active")));
        predicates.add(builder.or(
                builder.isNull(root.get("status")),
                builder.not(root.get("status").in(
                        AppointmentStatus.CANCELLED.getCode(),
                        AppointmentStatus.NO_SHOW.getCode()
                ))
        ));
        predicates.add(builder.greaterThanOrEqualTo(root.get("startTime"), startTime));
        predicates.add(builder.lessThanOrEqualTo(root.get("startTime"), endTime));

        if (excludeId != null) {
            predicates.add(builder.notEqual(root.get("id"), excludeId));
        }

        query.select(builder.count(root.get("id")));
        query.where(predicates.toArray(Predicate[]::new));

        Query<Long> hQuery = session.createQuery(query);

        return hQuery.getSingleResult();
    }

    @Override
    public List<Appointment> getBookableAppointmentsByDoctorAndDateRange(Long doctorId, Date fromDate, Date toDate) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("doctorId").get("id"), doctorId));
        predicates.add(builder.isTrue(root.get("active")));
        predicates.add(builder.or(
                builder.isNull(root.get("status")),
                builder.not(root.get("status").in(
                        AppointmentStatus.CANCELLED.getCode(),
                        AppointmentStatus.NO_SHOW.getCode()
                ))
        ));

        if (fromDate != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("appointmentDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("appointmentDate"), toDate));
        }

        query.select(root);
        query.where(predicates.toArray(Predicate[]::new));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Appointment getAppointmentById(Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);
        Fetch<Appointment, Doctor> doctorFetch = root.fetch("doctorId", JoinType.INNER);
        doctorFetch.fetch("departmentId", JoinType.LEFT);
        root.fetch("patientId", JoinType.INNER);
        root.fetch("serviceId", JoinType.LEFT);
        Fetch<Appointment, ?> medicalRecordFetch = root.fetch("medicalRecord", JoinType.LEFT);
        Fetch<?, ?> prescriptionFetch = medicalRecordFetch.fetch("prescription", JoinType.LEFT);
        Fetch<?, ?> itemFetch = prescriptionFetch.fetch("prescriptionItemSet", JoinType.LEFT);
        itemFetch.fetch("medicineId", JoinType.LEFT);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("id"), appointmentId),
                builder.isTrue(root.get("active"))
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Appointment getAppointmentByDoctorAndId(Long doctorId, Long appointmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Appointment> query = builder.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);

        root.fetch("patientId", JoinType.INNER);
        root.fetch("serviceId", JoinType.LEFT);
        Fetch<Appointment, ?> medicalRecordFetch = root.fetch("medicalRecord", JoinType.LEFT);
        Fetch<?, ?> prescriptionFetch = medicalRecordFetch.fetch("prescription", JoinType.LEFT);
        Fetch<?, ?> itemFetch = prescriptionFetch.fetch("prescriptionItemSet", JoinType.LEFT);
        itemFetch.fetch("medicineId", JoinType.LEFT);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("doctorId").get("id"), doctorId),
                builder.equal(root.get("id"), appointmentId),
                builder.isTrue(root.get("active"))
        );

        return session.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(appointment);
    }

    private List<Predicate> buildPatientPredicates(Long patientId, CriteriaBuilder builder, Root root, Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.isTrue(root.get("active")));
        predicates.add(builder.equal(root.get("patientId").get("id"), patientId));

        LocalDate defaultFrom = LocalDate.now();
        String from = defaultFrom.toString();
        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status.trim())) {
                predicates.add(builder.equal(
                        root.get("status"),
                        AppointmentStatus.normalize(status)
                ));
            }

            String fromParam = params.get("from");
            if (fromParam != null && !fromParam.isBlank()) {
                from = fromParam.trim();
            }

            String to = params.get("to");
            if (to != null && !to.isBlank()) {
                predicates.add(builder.lessThanOrEqualTo(
                        root.get("appointmentDate"),
                        java.sql.Date.valueOf(LocalDate.parse(to.trim()))
                ));
            }

            String serviceId = params.get("serviceId");
            if (serviceId != null && !serviceId.isBlank()) {
                predicates.add(builder.equal(
                        root.get("serviceId").get("id"),
                        Long.parseLong(serviceId.trim())
                ));
            }

            String doctorId = params.get("doctorId");
            if (doctorId != null && !doctorId.isBlank()) {
                predicates.add(builder.equal(
                        root.get("doctorId").get("id"),
                        Long.parseLong(doctorId.trim())
                ));
            }
        }

        predicates.add(builder.greaterThanOrEqualTo(
                root.get("appointmentDate"),
                java.sql.Date.valueOf(LocalDate.parse(from))
        ));
        return predicates;
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

            String serviceId = params.get("serviceId");
            if (serviceId != null && !serviceId.isBlank()) {
                predicates.add(builder.equal(
                        root.get("serviceId").get("id"),
                        Long.parseLong(serviceId.trim())
                ));
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
}

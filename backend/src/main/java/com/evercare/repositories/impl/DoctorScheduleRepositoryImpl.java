package com.evercare.repositories.impl;

import com.evercare.pojo.DoctorSchedule;
import com.evercare.repositories.DoctorScheduleRepository;
import com.evercare.utils.PaginationUtils;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class DoctorScheduleRepositoryImpl implements DoctorScheduleRepository {
    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    private List<Predicate> getPredicates(Map<String, String> params, CriteriaBuilder cb, Root root) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.isTrue(root.get("active")));

        if (params != null) {
            String doctorId = params.get("doctorId");
            if (doctorId != null && !doctorId.isBlank()) {
                predicates.add(cb.equal(
                        root.get("doctorId").get("id"),
                        Long.parseLong(doctorId)
                ));
            }

            String departmentId = params.get("departmentId");
            if (departmentId != null && !departmentId.isBlank()) {
                predicates.add(cb.equal(
                        root.get("doctorId").get("departmentId").get("id"),
                        Long.parseLong(departmentId)
                ));
            }

            String workDate = params.get("workDate");
            if (workDate != null && !workDate.isBlank()) {
                predicates.add(cb.equal(
                        root.get("workDate"),
                        LocalDate.parse(workDate)
                ));
            }

            String fromDate = params.get("fromDate");
            if (fromDate != null && !fromDate.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workDate"), LocalDate.parse(fromDate)));
            }

            String toDate = params.get("toDate");
            if (toDate != null && !toDate.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workDate"), LocalDate.parse(toDate)));
            }

            String status = params.get("status");
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
        }
        return predicates;
    }

    @Override
    public List<DoctorSchedule> getSchedules(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<DoctorSchedule> cq = cb.createQuery(DoctorSchedule.class);
        Root<DoctorSchedule> root = cq.from(DoctorSchedule.class);

        List<Predicate> predicates = getPredicates(params, cb, root);

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(
                cb.desc(root.get("workDate")),
                cb.asc(root.get("startTime"))
        );

        Query<DoctorSchedule> query = session.createQuery(cq);

        if (params != null && !Boolean.parseBoolean(params.getOrDefault("noPaging", "false"))) {
            int pageSize = this.env.getProperty("doctorSchedule.pageSize", Integer.class);
            int page = PaginationUtils.normalizePage(PaginationUtils.getPage(params), this.countDoctorSchedules(params), pageSize);
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public List<DoctorSchedule> getAvailableSchedulesByDoctorId(Long doctorId, LocalDate from, LocalDate to) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<DoctorSchedule> cq = cb.createQuery(DoctorSchedule.class);
        Root<DoctorSchedule> root = cq.from(DoctorSchedule.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.equal(root.get("doctorId").get("id"), doctorId));
        predicates.add(cb.equal(root.get("status"), "AVAILABLE"));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("workDate"), from));
        }

        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("workDate"), to));
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("workDate")), cb.asc(root.get("startTime")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    public DoctorSchedule getScheduleCoveringAppointmentTime(Long doctorId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT s
                FROM DoctorSchedule s
                JOIN FETCH s.doctorId d
                WHERE d.id = :doctorId
                    AND s.workDate = :workDate
                    AND s.active = true
                    AND s.status = 'AVAILABLE'
                    AND s.startTime <= :startTime
                    AND s.endTime >= :endTime
                ORDER BY s.startTime ASC
                """, DoctorSchedule.class)
                .setParameter("doctorId", doctorId)
                .setParameter("workDate", workDate)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public DoctorSchedule getScheduleById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(DoctorSchedule.class, Long.valueOf(id));
    }

    @Override
    public void addSchedule(DoctorSchedule schedule) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(schedule);
    }

    @Override
    public void updateSchedule(DoctorSchedule schedule) {
        Session session = this.factory.getObject().getCurrentSession();
        session.merge(schedule);
    }

    @Override
    public boolean existsOverlappingSchedule(Long doctorId,
                                             LocalDate workDate,
                                             LocalTime startTime,
                                             LocalTime endTime,
                                             Long excludeId) {
        Session session = this.factory.getObject().getCurrentSession();

        String hql = """
                SELECT COUNT(s.id)
                FROM DoctorSchedule s
                WHERE s.doctorId.id = :doctorId
                  AND s.workDate = :workDate
                  AND s.active = true
                  AND s.startTime < :endTime
                  AND s.endTime > :startTime
                """;

        if (excludeId != null) {
            hql += " AND s.id <> :excludeId";
        }

        var query = session.createQuery(hql, Long.class);
        query.setParameter("doctorId", doctorId);
        query.setParameter("workDate", workDate);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);

        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        return query.getSingleResult() > 0;
    }

    @Override
    public long countDoctorSchedules(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<DoctorSchedule> root = cq.from(DoctorSchedule.class);
        cq.select(cb.count(root));

        List<Predicate> predicates = getPredicates(params, cb, root);

        cq.where(predicates.toArray(Predicate[]::new));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        long count = this.countDoctorSchedules(params);
        int pageSize = Integer.parseInt(env.getProperty("doctorSchedule.pageSize"));

        return (long) Math.max(1, Math.ceil((double) count / pageSize));
    }
}

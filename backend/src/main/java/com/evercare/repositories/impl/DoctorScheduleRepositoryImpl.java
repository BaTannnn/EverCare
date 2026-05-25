package com.evercare.repositories.impl;

import com.evercare.pojo.DoctorSchedule;
import com.evercare.repositories.DoctorScheduleRepository;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LocalSessionFactoryBean factory;

    @Override
    public List<DoctorSchedule> getSchedules(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<DoctorSchedule> cq = cb.createQuery(DoctorSchedule.class);
        Root<DoctorSchedule> root = cq.from(DoctorSchedule.class);

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

            String status = params.get("status");
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(
                cb.desc(root.get("workDate")),
                cb.asc(root.get("startTime"))
        );

        return session.createQuery(cq).getResultList();
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
}
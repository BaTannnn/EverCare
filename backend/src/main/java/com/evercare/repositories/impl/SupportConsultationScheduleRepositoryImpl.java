package com.evercare.repositories.impl;

import com.evercare.pojo.SupportConsultationSchedule;
import com.evercare.repositories.SupportConsultationScheduleRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SupportConsultationScheduleRepositoryImpl implements SupportConsultationScheduleRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<SupportConsultationSchedule> getSchedulesByConversationId(Long conversationId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportConsultationSchedule> query = builder.createQuery(SupportConsultationSchedule.class);
        Root<SupportConsultationSchedule> root = query.from(SupportConsultationSchedule.class);
        root.fetch("doctorId");
        root.fetch("patientId");
        root.fetch("staffId", jakarta.persistence.criteria.JoinType.LEFT);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("conversationId").get("id"), conversationId),
                builder.isTrue(root.get("active"))
        );
        query.orderBy(builder.desc(root.get("scheduledStart")), builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public SupportConsultationSchedule save(SupportConsultationSchedule schedule) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(schedule);
        session.flush();
        return schedule;
    }
}

package com.evercare.repositories.impl;

import com.evercare.pojo.Patient;
import com.evercare.pojo.SupportConversation;
import com.evercare.repositories.SupportConversationRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SupportConversationRepositoryImpl implements SupportConversationRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public SupportConversation getConversationById(Long conversationId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportConversation> query = builder.createQuery(SupportConversation.class);
        Root<SupportConversation> root = query.from(SupportConversation.class);
        fetchConversationGraph(root);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("id"), conversationId),
                builder.isTrue(root.get("active"))
        );

        return session.createQuery(query).uniqueResult();
    }

    @Override
    public List<SupportConversation> getConversationsByPatientId(Long patientId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportConversation> query = builder.createQuery(SupportConversation.class);
        Root<SupportConversation> root = query.from(SupportConversation.class);
        fetchConversationGraph(root);

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("patientId").get("id"), patientId),
                builder.isTrue(root.get("active"))
        );
        query.orderBy(
                builder.desc(root.get("createdAt")),
                builder.desc(root.get("id"))
        );

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<SupportConversation> getConversationsForReceptionist(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportConversation> query = builder.createQuery(SupportConversation.class);
        Root<SupportConversation> root = query.from(SupportConversation.class);
        fetchConversationGraph(root);
        Join<SupportConversation, Patient> patientJoin = root.join("patientId", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.isTrue(root.get("active")));

        if (params != null) {
            String status = trimToNull(params.get("status"));
            if (status != null) {
                predicates.add(builder.equal(builder.upper(root.get("status")), status.toUpperCase()));
            }

            String keyword = trimToNull(params.get("keyword"));
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(patientJoin.get("fullName")), like),
                        builder.like(builder.lower(patientJoin.get("phone")), like),
                        builder.like(builder.lower(root.<String>get("subject")), like)
                ));
            }
        }

        Expression<Integer> statusOrder = builder.<Integer>selectCase()
                .when(builder.equal(root.get("status"), "OPEN"), 0)
                .when(builder.equal(root.get("status"), "IN_PROGRESS"), 1)
                .when(builder.equal(root.get("status"), "ASSIGNED"), 2)
                .otherwise(3);

        query.select(root).distinct(true);
        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(
                builder.asc(statusOrder),
                builder.desc(root.get("createdAt")),
                builder.desc(root.get("id"))
        );

        return session.createQuery(query).getResultList();
    }

    private void fetchConversationGraph(Root<SupportConversation> root) {
        Fetch<?, ?> patientUserFetch = root.fetch("patientId", JoinType.INNER)
                .fetch("userId", JoinType.LEFT);
        fetchUserProfileGraph(patientUserFetch);
        root.fetch("staffId", JoinType.LEFT);
    }

    private void fetchUserProfileGraph(Fetch<?, ?> userFetch) {
        userFetch.fetch("patient", JoinType.LEFT);
        userFetch.fetch("employee", JoinType.LEFT);
        userFetch.fetch("doctor", JoinType.LEFT);
    }

    @Override
    public SupportConversation save(SupportConversation conversation) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(conversation);
        session.flush();
        return conversation;
    }

    @Override
    public SupportConversation update(SupportConversation conversation) {
        Session session = this.factory.getObject().getCurrentSession();
        SupportConversation updated = (SupportConversation) session.merge(conversation);
        session.flush();
        return updated;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

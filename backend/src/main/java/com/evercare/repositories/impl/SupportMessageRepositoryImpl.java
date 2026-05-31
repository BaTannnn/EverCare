package com.evercare.repositories.impl;

import com.evercare.pojo.SupportMessage;
import com.evercare.repositories.SupportMessageRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SupportMessageRepositoryImpl implements SupportMessageRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<SupportMessage> getMessagesByConversationId(Long conversationId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportMessage> query = builder.createQuery(SupportMessage.class);
        Root<SupportMessage> root = query.from(SupportMessage.class);
        root.fetch("senderId");

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("conversationId").get("id"), conversationId),
                builder.isTrue(root.get("active"))
        );
        query.orderBy(builder.asc(root.get("createdAt")), builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public SupportMessage getLatestMessageByConversationId(Long conversationId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportMessage> query = builder.createQuery(SupportMessage.class);
        Root<SupportMessage> root = query.from(SupportMessage.class);
        root.fetch("senderId");

        query.select(root).distinct(true);
        query.where(
                builder.equal(root.get("conversationId").get("id"), conversationId),
                builder.isTrue(root.get("active"))
        );
        query.orderBy(builder.desc(root.get("createdAt")), builder.desc(root.get("id")));

        return session.createQuery(query)
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public long countUnreadMessages(Long conversationId, Long readerUserId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<SupportMessage> root = query.from(SupportMessage.class);

        Predicate unread = builder.or(
                builder.isFalse(root.get("read")),
                builder.isNull(root.get("read"))
        );

        query.select(builder.count(root));
        query.where(
                builder.equal(root.get("conversationId").get("id"), conversationId),
                builder.notEqual(root.get("senderId").get("id"), readerUserId),
                builder.isTrue(root.get("active")),
                unread
        );

        Long count = session.createQuery(query).uniqueResult();
        return count != null ? count : 0L;
    }

    @Override
    public void markMessagesRead(Long conversationId, Long readerUserId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaUpdate<SupportMessage> update = builder.createCriteriaUpdate(SupportMessage.class);
        Root<SupportMessage> root = update.from(SupportMessage.class);

        Predicate unread = builder.or(
                builder.isFalse(root.get("read")),
                builder.isNull(root.get("read"))
        );

        update.set("read", true);
        update.where(
                builder.equal(root.get("conversationId").get("id"), conversationId),
                builder.notEqual(root.get("senderId").get("id"), readerUserId),
                builder.isTrue(root.get("active")),
                unread
        );

        session.createMutationQuery(update).executeUpdate();
    }

    @Override
    public SupportMessage save(SupportMessage message) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(message);
        session.flush();
        return message;
    }
}

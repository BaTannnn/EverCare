package com.evercare.repositories.impl;

import com.evercare.pojo.SupportMessage;
import com.evercare.repositories.SupportMessageRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SupportMessageRepositoryImpl implements SupportMessageRepository {

    private static final int DEFAULT_MESSAGE_LIMIT = 50;
    private static final int MAX_MESSAGE_LIMIT = 100;

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<SupportMessage> getMessagesByConversationId(Long conversationId) {
        return getMessagesByConversationId(conversationId, null, DEFAULT_MESSAGE_LIMIT);
    }

    @Override
    public List<SupportMessage> getMessagesByConversationId(Long conversationId, Long afterId, Integer limit) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportMessage> query = builder.createQuery(SupportMessage.class);
        Root<SupportMessage> root = query.from(SupportMessage.class);
        fetchMessageGraph(root);

        int normalizedLimit = normalizeLimit(limit);
        boolean onlyNewMessages = afterId != null && afterId > 0;
        Predicate activeConversation = builder.equal(root.get("conversationId").get("id"), conversationId);
        Predicate activeMessage = builder.isTrue(root.get("active"));

        query.select(root).distinct(true);
        if (onlyNewMessages) {
            query.where(
                    activeConversation,
                    activeMessage,
                    builder.greaterThan(root.<Long>get("id"), afterId)
            );
            query.orderBy(builder.asc(root.get("createdAt")), builder.asc(root.get("id")));

            return session.createQuery(query)
                    .setMaxResults(normalizedLimit)
                    .getResultList();
        }

        query.where(activeConversation, activeMessage);
        query.orderBy(builder.desc(root.get("createdAt")), builder.desc(root.get("id")));

        List<SupportMessage> messages = session.createQuery(query)
                .setMaxResults(normalizedLimit)
                .getResultList();
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public Map<Long, SupportMessage> getLatestMessagesByConversationIds(List<Long> conversationIds) {
        List<Long> ids = normalizeIds(conversationIds);
        Map<Long, SupportMessage> latestMessages = new HashMap<>();
        if (ids.isEmpty()) {
            return latestMessages;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportMessage> query = builder.createQuery(SupportMessage.class);
        Root<SupportMessage> root = query.from(SupportMessage.class);
        fetchMessageGraph(root);

        Subquery<Long> latestMessageIds = query.subquery(Long.class);
        Root<SupportMessage> subRoot = latestMessageIds.from(SupportMessage.class);
        latestMessageIds.select(builder.max(subRoot.<Long>get("id")));
        latestMessageIds.where(
                subRoot.get("conversationId").get("id").in(ids),
                builder.isTrue(subRoot.get("active"))
        );
        latestMessageIds.groupBy(subRoot.get("conversationId").get("id"));

        query.select(root).distinct(true);
        query.where(root.get("id").in(latestMessageIds));

        for (SupportMessage message : session.createQuery(query).getResultList()) {
            if (message.getConversationId() != null) {
                latestMessages.put(message.getConversationId().getId(), message);
            }
        }

        return latestMessages;
    }

    @Override
    public SupportMessage getLatestMessageByConversationId(Long conversationId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<SupportMessage> query = builder.createQuery(SupportMessage.class);
        Root<SupportMessage> root = query.from(SupportMessage.class);
        fetchMessageGraph(root);

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
        return countUnreadMessagesByConversationIds(List.of(conversationId), readerUserId)
                .getOrDefault(conversationId, 0L);
    }

    @Override
    public Map<Long, Long> countUnreadMessagesByConversationIds(List<Long> conversationIds, Long readerUserId) {
        List<Long> ids = normalizeIds(conversationIds);
        Map<Long, Long> unreadCounts = new HashMap<>();
        for (Long id : ids) {
            unreadCounts.put(id, 0L);
        }

        if (ids.isEmpty() || readerUserId == null) {
            return unreadCounts;
        }

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
        Root<SupportMessage> root = query.from(SupportMessage.class);

        Predicate unread = builder.or(
                builder.isFalse(root.get("read")),
                builder.isNull(root.get("read"))
        );

        query.multiselect(root.get("conversationId").get("id"), builder.count(root));
        query.where(
                root.get("conversationId").get("id").in(ids),
                builder.notEqual(root.get("senderId").get("id"), readerUserId),
                builder.isTrue(root.get("active")),
                unread
        );
        query.groupBy(root.get("conversationId").get("id"));

        for (Object[] row : session.createQuery(query).getResultList()) {
            Long conversationId = (Long) row[0];
            Number count = (Number) row[1];
            unreadCounts.put(conversationId, count != null ? count.longValue() : 0L);
        }

        return unreadCounts;
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

    private void fetchMessageGraph(Root<SupportMessage> root) {
        Fetch<?, ?> senderFetch = root.fetch("senderId");
        senderFetch.fetch("patient", JoinType.LEFT);
        senderFetch.fetch("employee", JoinType.LEFT);
        senderFetch.fetch("doctor", JoinType.LEFT);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_MESSAGE_LIMIT;
        }

        return Math.min(limit, MAX_MESSAGE_LIMIT);
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }

        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}

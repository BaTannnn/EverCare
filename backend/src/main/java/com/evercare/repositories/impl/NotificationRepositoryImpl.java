package com.evercare.repositories.impl;

import com.evercare.pojo.Notification;
import com.evercare.repositories.NotificationRepository;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class NotificationRepositoryImpl implements NotificationRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Notification createNotification(Notification notification) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(notification);
        session.flush();
        return notification;
    }

    @Override
    public List<Notification> getNotificationsByUserId(Long userId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT n
                FROM Notification n
                WHERE n.active = true
                    AND n.userId.id = :userId
                ORDER BY n.createdAt DESC, n.id DESC
                """, Notification.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Notification getNotificationByUserIdAndId(Long userId, Long id) {
        Session session = this.factory.getObject().getCurrentSession();

        return session.createQuery("""
                SELECT n
                FROM Notification n
                WHERE n.active = true
                    AND n.userId.id = :userId
                    AND n.id = :id
                """, Notification.class)
                .setParameter("userId", userId)
                .setParameter("id", id)
                .uniqueResult();
    }

    @Override
    public Notification updateNotification(Notification notification) {
        Session session = this.factory.getObject().getCurrentSession();
        notification = (Notification) session.merge(notification);
        session.flush();
        return notification;
    }
}

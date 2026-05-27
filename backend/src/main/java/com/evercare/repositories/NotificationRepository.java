package com.evercare.repositories;

import com.evercare.pojo.Notification;
import java.util.List;
import java.util.Map;

public interface NotificationRepository {
    Notification createNotification(Notification notification);
    List<Notification> getNotificationsByUserId(Long userId, Map<String, String> params);
    Notification getNotificationByUserIdAndId(Long userId, Long id);
    Notification updateNotification(Notification notification);
}

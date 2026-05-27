package com.evercare.mappers;

import com.evercare.dtos.response.NotificationResponse;
import com.evercare.pojo.Notification;
import java.text.SimpleDateFormat;

public final class NotificationMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationResponse res = new NotificationResponse();
        res.setId(notification.getId());
        res.setTitle(notification.getTitle());
        res.setContent(notification.getContent());
        res.setNotificationType(notification.getNotificationType());
        res.setRelatedId(notification.getRelatedId() != null ? notification.getRelatedId().longValue() : null);
        res.setReadAt(format(notification.getReadAt()));
        res.setCreatedAt(format(notification.getCreatedAt()));
        res.setActive(notification.getActive());

        return res;
    }

    private static String format(java.util.Date date) {
        return date == null ? null : new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }
}

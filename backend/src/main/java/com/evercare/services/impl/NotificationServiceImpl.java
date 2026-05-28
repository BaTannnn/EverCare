package com.evercare.services.impl;

import com.evercare.dtos.response.NotificationResponse;
import com.evercare.mappers.NotificationMapper;
import com.evercare.pojo.Notification;
import com.evercare.pojo.User;
import com.evercare.repositories.NotificationRepository;
import com.evercare.services.NotificationService;
import com.evercare.services.UserService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<NotificationResponse> list(Map<String, String> params) {
        User user = getCurrentUser();
        List<NotificationResponse> result = new ArrayList<>();
        for (Notification notification : this.notificationRepo.getNotificationsByUserId(user.getId(), params)) {
            result.add(NotificationMapper.toResponse(notification));
        }
        return result;
    }

    @Override
    public NotificationResponse markRead(Long id) {
        User user = getCurrentUser();
        Notification notification = this.notificationRepo.getNotificationByUserIdAndId(user.getId(), id);
        if (notification == null) {
            throw new NoSuchElementException("Không tìm thấy thông báo");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(new Date());
            notification = this.notificationRepo.updateNotification(notification);
        }

        return NotificationMapper.toResponse(notification);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản không hợp lệ");
        }

        return user;
    }
}

package com.evercare.services;

import com.evercare.dtos.response.NotificationResponse;
import java.util.List;
import java.util.Map;

public interface NotificationService {
    List<NotificationResponse> list(Map<String, String> params);
    NotificationResponse markRead(Long id);
}

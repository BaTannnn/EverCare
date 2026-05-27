package com.evercare.controllers.api;

import com.evercare.dtos.response.NotificationResponse;
import com.evercare.services.NotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/notifications")
@CrossOrigin
public class ApiNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list() {
        return ResponseEntity.ok(this.notificationService.list(Map.of()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> update(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.notificationService.markRead(id));
    }
}

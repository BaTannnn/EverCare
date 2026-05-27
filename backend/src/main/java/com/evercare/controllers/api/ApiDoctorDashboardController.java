package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorDashboardSummaryResponse;
import com.evercare.services.DoctorDashboardService;
import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor/dashboard")
@CrossOrigin
public class ApiDoctorDashboardController {
    @Autowired
    private DoctorDashboardService doctorDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<?> summary(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            DoctorDashboardSummaryResponse result = this.doctorDashboardService.getTodaySummary(principal.getName());
            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        }
    }
}

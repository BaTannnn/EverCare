package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorDashboardSummaryResponse;
import com.evercare.services.DoctorDashboardService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
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
        DoctorDashboardSummaryResponse result = this.doctorDashboardService.getTodaySummary(requireUsername(principal));
        return ResponseEntity.ok(result);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}

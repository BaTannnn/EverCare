package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorScheduleResponse;
import com.evercare.services.DoctorScheduleService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor/schedules")
@CrossOrigin
public class ApiDoctorScheduleController {
    @Autowired
    private DoctorScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            List<DoctorScheduleResponse> result = this.scheduleService
                    .getCurrentDoctorSchedules(principal.getName(), params);

            return ResponseEntity.ok(result);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ngày không hợp lệ, định dạng đúng là yyyy-MM-dd"));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> today(Principal principal) {
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().toString());

        return list(principal, params);
    }
}

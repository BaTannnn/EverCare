package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorAppointmentResponse;
import com.evercare.services.DoctorAppointmentService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor/appointments")
@CrossOrigin
public class ApiDoctorAppointmentController {
    @Autowired
    private DoctorAppointmentService doctorAppointmentService;

    @GetMapping("/today")
    public ResponseEntity<?> today(Principal principal) {
        return appointmentsByDate(principal, LocalDate.now());
    }

    @GetMapping
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam("date") String date
    ) {
        try {
            return appointmentsByDate(principal, LocalDate.parse(date));
        } catch (DateTimeParseException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Ngày không hợp lệ, định dạng đúng là yyyy-MM-dd"));
        }
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> detail(
            Principal principal,
            @PathVariable("appointmentId") Long appointmentId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            DoctorAppointmentResponse result = this.doctorAppointmentService
                    .getAppointmentById(principal.getName(), appointmentId);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    private ResponseEntity<?> appointmentsByDate(Principal principal, LocalDate date) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            List<DoctorAppointmentResponse> result = this.doctorAppointmentService
                    .getAppointmentsByDate(principal.getName(), date);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        }
    }
}

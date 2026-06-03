package com.evercare.controllers.api;

import com.evercare.dtos.request.StartExaminationRequest;
import com.evercare.dtos.response.DoctorAppointmentResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.services.DoctorAppointmentService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        return appointmentsByDate(principal, LocalDate.parse(date));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> detail(
            Principal principal,
            @PathVariable("appointmentId") Long appointmentId
    ) {
        DoctorAppointmentResponse result = this.doctorAppointmentService
                .getAppointmentById(requireUsername(principal), appointmentId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{appointmentId}/start-examination")
    public ResponseEntity<?> startExamination(
            Principal principal,
            @PathVariable("appointmentId") Long appointmentId,
            @RequestBody(required = false) StartExaminationRequest request
    ) {
        MedicalRecordResponse result = this.doctorAppointmentService
                .startExamination(requireUsername(principal), appointmentId, request);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<?> appointmentsByDate(Principal principal, LocalDate date) {
        List<DoctorAppointmentResponse> result = this.doctorAppointmentService
                .getAppointmentsByDate(requireUsername(principal), date);
        return ResponseEntity.ok(result);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}

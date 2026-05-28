package com.evercare.controllers.api;

import com.evercare.dtos.request.AppointmentCancelRequest;
import com.evercare.dtos.request.AppointmentRequest;
import com.evercare.dtos.response.AppointmentResponse;
import com.evercare.services.AppointmentService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
@CrossOrigin
public class ApiAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentRequest request) {
        AppointmentResponse response = this.appointmentService.bookAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping({"/appointments", "/patients/{patientId}/appointments"})
    public ResponseEntity<List<AppointmentResponse>> list(
            @PathVariable(value = "patientId", required = false) Long patientId,
            @RequestParam Map<String, String> params
    ) {
        List<AppointmentResponse> response = this.appointmentService.getAppointmentsByCurrentPatient(params);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<AppointmentResponse> retrieve(@PathVariable("appointmentId") Long appointmentId) {
        AppointmentResponse response = this.appointmentService.getAppointmentByCurrentPatient(appointmentId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse> destroy(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestBody(required = false) AppointmentCancelRequest request
    ) {
        AppointmentResponse response = this.appointmentService.cancelAppointment(appointmentId, request);
        return ResponseEntity.ok(response);
    }
}

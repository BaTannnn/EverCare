package com.evercare.controllers.api;

import com.evercare.dtos.request.AppointmentRequest;
import com.evercare.dtos.request.CheckInRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/receptionist/appointments")
@CrossOrigin
public class ApiReceptionistAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> list(@RequestParam Map<String, String> params) {
        List<AppointmentResponse> response = this.appointmentService.getAppointmentsForReceptionist(params);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> retrieve(@PathVariable("appointmentId") Long appointmentId) {
        AppointmentResponse response = this.appointmentService.getAppointmentForReceptionist(appointmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody AppointmentRequest request) {
        AppointmentResponse response = this.appointmentService.createAppointmentForReceptionist(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestBody AppointmentRequest request
    ) {
        AppointmentResponse response = this.appointmentService.updateAppointmentForReceptionist(appointmentId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{appointmentId}/check-in")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable("appointmentId") Long appointmentId,
            @RequestBody(required = false) CheckInRequest request
    ) {
        AppointmentResponse response = this.appointmentService.checkInAppointment(appointmentId, request);
        return ResponseEntity.ok(response);
    }
}

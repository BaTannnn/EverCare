package com.evercare.controllers.api;

import com.evercare.dtos.request.PrescriptionRequest;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.services.DoctorPrescriptionService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin
public class ApiDoctorPrescriptionController {
    @Autowired
    private DoctorPrescriptionService doctorPrescriptionService;

    @GetMapping("/prescriptions")
    public ResponseEntity<?> list(
            Principal principal,
            @org.springframework.web.bind.annotation.RequestParam Map<String, String> params
    ) {
        List<PrescriptionResponse> result = this.doctorPrescriptionService
                .getPrescriptions(requireUsername(principal), params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<?> retrieve(
            Principal principal,
            @PathVariable("prescriptionId") Long prescriptionId
    ) {
        return ResponseEntity.ok(this.doctorPrescriptionService.getPrescription(requireUsername(principal), prescriptionId));
    }

    @PostMapping("/medical-records/{recordId}/prescriptions")
    public ResponseEntity<?> create(
            Principal principal,
            @PathVariable("recordId") Long recordId,
            @RequestBody PrescriptionRequest request
    ) {
        PrescriptionResponse result = this.doctorPrescriptionService
                .createPrescription(requireUsername(principal), recordId, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<?> update(
            Principal principal,
            @PathVariable("prescriptionId") Long prescriptionId,
            @RequestBody PrescriptionRequest request
    ) {
        PrescriptionResponse result = this.doctorPrescriptionService
                .updatePrescription(requireUsername(principal), prescriptionId, request);
        return ResponseEntity.ok(result);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}

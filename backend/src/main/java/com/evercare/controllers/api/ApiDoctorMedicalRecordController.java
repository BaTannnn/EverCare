package com.evercare.controllers.api;

import com.evercare.dtos.request.MedicalRecordServiceRequest;
import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.services.DoctorMedicalRecordService;
import java.security.Principal;
import java.util.List;
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
@RequestMapping("/api/doctor/medical-records")
@CrossOrigin
public class ApiDoctorMedicalRecordController {
    @Autowired
    private DoctorMedicalRecordService doctorMedicalRecordService;

    @PutMapping("/{recordId}")
    public ResponseEntity<?> update(
            Principal principal,
            @PathVariable("recordId") Long recordId,
            @RequestBody(required = false) UpdateMedicalRecordRequest request
    ) {
        MedicalRecordResponse result = this.doctorMedicalRecordService
                .updateMedicalRecord(requireUsername(principal), recordId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{recordId}/complete")
    public ResponseEntity<?> complete(
            Principal principal,
            @PathVariable("recordId") Long recordId
    ) {
        MedicalRecordResponse result = this.doctorMedicalRecordService
                .completeMedicalRecord(requireUsername(principal), recordId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{recordId}/services")
    public ResponseEntity<?> addService(
            Principal principal,
            @PathVariable("recordId") Long recordId,
            @RequestBody MedicalRecordServiceRequest request
    ) {
        MedicalRecordServiceResponse result = this.doctorMedicalRecordService
                .addService(requireUsername(principal), recordId, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{recordId}/services")
    public ResponseEntity<?> getServices(
            Principal principal,
            @PathVariable("recordId") Long recordId
    ) {
        List<MedicalRecordServiceResponse> result = this.doctorMedicalRecordService
                .getServices(requireUsername(principal), recordId);
        return ResponseEntity.ok(result);
    }

    private String requireUsername(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }
        return principal.getName();
    }
}

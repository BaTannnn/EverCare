package com.evercare.controllers.api;

import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.services.DoctorMedicalRecordService;
import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
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
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        try {
            MedicalRecordResponse result = this.doctorMedicalRecordService
                    .updateMedicalRecord(principal.getName(), recordId, request);

            return ResponseEntity.ok(result);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }
}

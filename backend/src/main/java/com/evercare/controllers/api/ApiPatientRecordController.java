package com.evercare.controllers.api;

import com.evercare.dtos.response.MedicalRecordDetailResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.services.PatientRecordService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/patient")
@CrossOrigin
public class ApiPatientRecordController {
    @Autowired
    private PatientRecordService patientRecordService;

    @GetMapping("/medical-records")
    public ResponseEntity<List<MedicalRecordResponse>> listMedicalRecords(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(this.patientRecordService.getMedicalRecordsByCurrentPatient(params));
    }

    @GetMapping("/medical-records/{recordId}")
    public ResponseEntity<MedicalRecordDetailResponse> retrieveMedicalRecord(@PathVariable("recordId") Long recordId) {
        return ResponseEntity.ok(this.patientRecordService.getMedicalRecordByCurrentPatient(recordId));
    }

    @GetMapping("/test-results")
    public ResponseEntity<List<TestResultResponse>> listTestResults(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(this.patientRecordService.getTestResultsByCurrentPatient(params));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<List<PrescriptionResponse>> listPrescriptions(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(this.patientRecordService.getPrescriptionsByCurrentPatient(params));
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<PrescriptionResponse> retrievePrescription(@PathVariable("prescriptionId") Long prescriptionId) {
        return ResponseEntity.ok(this.patientRecordService.getPrescriptionByCurrentPatient(prescriptionId));
    }
}

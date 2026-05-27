package com.evercare.controllers.api;

import com.evercare.dtos.request.PatientRequest;
import com.evercare.dtos.response.PatientResponse;
import com.evercare.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/secure/patients")
@CrossOrigin
public class ApiPatientController {
    @Autowired
    private PatientService patientService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponse> create(@RequestBody PatientRequest request) {
        PatientResponse response = this.patientService.createProfile(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{patientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponse> getMyProfile(
            @PathVariable("patientId") Long patientId
    ) {
        PatientResponse response = this.patientService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{patientId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponse> update(
            @PathVariable("patientId") Long patientId,
            @RequestBody PatientRequest request
    ) {
        PatientResponse response = this.patientService.updateMyProfile(request);
        return ResponseEntity.ok(response);
    }
}

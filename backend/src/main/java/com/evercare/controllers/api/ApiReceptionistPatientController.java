package com.evercare.controllers.api;

import com.evercare.dtos.response.PatientResponse;
import com.evercare.services.PatientService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure/receptionist/patients")
@CrossOrigin
public class ApiReceptionistPatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(this.patientService.searchForReceptionist(keyword, limit));
    }
}

package com.evercare.controllers.api;

import com.evercare.dtos.response.MedicalServiceResponse;
import com.evercare.enums.MedicalServiceType;
import com.evercare.mappers.MedicalServiceMapper;
import com.evercare.services.MedicalServiceService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-services")
@CrossOrigin
public class ApiMedicalServiceController {

    @Autowired
    private MedicalServiceService medicalServiceService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Map<String, String> params) {
        Map<String, String> effectiveParams = new HashMap<>();
        if (params != null) {
            effectiveParams.putAll(params);
        }

        List<MedicalServiceResponse> result = new ArrayList<>();
        for (var service : this.medicalServiceService.getServices(effectiveParams)) {
            result.add(MedicalServiceMapper.toResponse(service));
        }

        return ResponseEntity.ok(result);
    }
}

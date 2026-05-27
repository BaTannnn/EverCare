package com.evercare.controllers.api;

import com.evercare.dtos.response.DoctorResponse;
import com.evercare.mappers.DoctorMapper;
import com.evercare.services.DoctorService;
import java.security.Principal;
import java.util.ArrayList;
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
@RequestMapping({ "/api/doctors"})
@CrossOrigin
public class ApiDoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Map<String, String> params) {
        List<DoctorResponse> result = new ArrayList<>();
        for (var doctor : this.doctorService.getDoctors(params)) {
            result.add(DoctorMapper.toResponse(doctor));
        }

        return ResponseEntity.ok(result);
    }
}

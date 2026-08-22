package com.evercare.controllers.api;

import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.pojo.Employee;
import com.evercare.services.PrescriptionService;
import com.evercare.utils.AuthSupport;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacist/prescriptions")
@CrossOrigin
public class ApiPharmacistPrescriptionController {
    @Autowired
    private PrescriptionService prescriptionService;
    @Autowired
    private AuthSupport authSupport;
    @GetMapping
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");

        List<PrescriptionResponse> result = this.prescriptionService.getPrescriptions(params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.prescriptionService.getPrescriptionById(id));
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<?> dispense(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        Employee employee = this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.prescriptionService.dispensePrescription(employee.getUserId(), id));
    }
}

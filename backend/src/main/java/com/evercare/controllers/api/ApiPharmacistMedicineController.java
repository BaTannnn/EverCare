package com.evercare.controllers.api;

import com.evercare.dtos.request.MedicineRequest;
import com.evercare.dtos.request.MedicineStatusRequest;
import com.evercare.dtos.response.MedicineLowStockResponse;
import com.evercare.dtos.response.MedicineResponse;
import com.evercare.services.MedicineService;
import com.evercare.utils.AuthSupport;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/pharmacist/medicines")
@CrossOrigin
public class ApiPharmacistMedicineController {
    @Autowired
    private MedicineService medicineService;
    @Autowired
    private AuthSupport authSupport;
    @GetMapping
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.medicineService.getMedicines(params));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockMedicines(Principal principal) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");

        List<MedicineLowStockResponse> result = this.medicineService.getLowStockMedicines();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(
            Principal principal,
            @RequestBody MedicineRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        MedicineResponse result = this.medicineService.createMedicine(request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody MedicineRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.medicineService.updateMedicine(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody MedicineStatusRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.medicineService.updateStatus(id, request != null ? request.getActive() : null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        this.medicineService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}

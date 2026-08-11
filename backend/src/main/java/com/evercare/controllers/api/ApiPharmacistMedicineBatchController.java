package com.evercare.controllers.api;

import com.evercare.dtos.request.MedicineBatchImportRequest;
import com.evercare.dtos.response.MedicineBatchImportResponse;
import com.evercare.dtos.response.MedicineBatchResponse;
import com.evercare.services.MedicineBatchService;
import com.evercare.utils.AuthSupport;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacist")
@CrossOrigin
public class ApiPharmacistMedicineBatchController {
    @Autowired
    private MedicineBatchService medicineBatchService;
    @Autowired
    private AuthSupport authSupport;
    @PostMapping("/medicine-batches/import")
    public ResponseEntity<?> importBatch(
            Principal principal,
            @RequestBody MedicineBatchImportRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        MedicineBatchImportResponse result = this.medicineBatchService.importBatch(username, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/medicine-batches")
    public ResponseEntity<?> getBatches(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");

        List<MedicineBatchResponse> result = this.medicineBatchService.getBatches(params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicine-batches/near-expiry")
    public ResponseEntity<?> getNearExpiryBatches(
            Principal principal,
            @RequestParam(value = "days", required = false) Integer days
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        List<MedicineBatchResponse> result = this.medicineBatchService.getNearExpiryBatches(days);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicine-batches/expired")
    public ResponseEntity<?> getExpiredBatches(Principal principal) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");

        List<MedicineBatchResponse> result = this.medicineBatchService.getExpiredBatches();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicines/{medicineId}/batches")
    public ResponseEntity<?> getBatchesByMedicine(
            Principal principal,
            @PathVariable("medicineId") Long medicineId
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        List<MedicineBatchResponse> result = this.medicineBatchService.getBatchesByMedicineId(medicineId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicine-batches/{id}")
    public ResponseEntity<?> getBatchDetail(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.medicineBatchService.getBatchById(id));
    }

    @PutMapping("/medicine-batches/{id}")
    public ResponseEntity<?> updateBatch(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody MedicineBatchImportRequest request
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        return ResponseEntity.ok(this.medicineBatchService.updateBatch(id, request));
    }

    @DeleteMapping("/medicine-batches/{id}")
    public ResponseEntity<?> deleteBatch(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        String username = this.authSupport.requireUsername(principal);
        this.authSupport.requireCurrentEmployee(username, "PHARMACIST", "Tài khoản hiện tại không phải dược sĩ đang hoạt động");
        this.medicineBatchService.deleteBatch(id);
        return ResponseEntity.noContent().build();
    }
}

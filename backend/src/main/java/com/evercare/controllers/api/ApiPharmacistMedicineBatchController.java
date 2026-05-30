package com.evercare.controllers.api;

import com.evercare.dtos.request.MedicineBatchImportRequest;
import com.evercare.dtos.response.MedicineBatchImportResponse;
import com.evercare.dtos.response.MedicineBatchResponse;
import com.evercare.pojo.Employee;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.services.MedicineBatchService;
import com.evercare.services.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private UserService userService;

    @Autowired
    private EmployeeRepository employeeRepo;

    @PostMapping("/medicine-batches/import")
    public ResponseEntity<?> importBatch(
            Principal principal,
            @RequestBody MedicineBatchImportRequest request
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            MedicineBatchImportResponse result = this.medicineBatchService.importBatch(principal.getName(), request);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/medicine-batches")
    public ResponseEntity<?> getBatches(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        List<MedicineBatchResponse> result = this.medicineBatchService.getBatches(params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicine-batches/near-expiry")
    public ResponseEntity<?> getNearExpiryBatches(
            Principal principal,
            @RequestParam(value = "days", required = false) Integer days
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            List<MedicineBatchResponse> result = this.medicineBatchService.getNearExpiryBatches(days);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/medicine-batches/expired")
    public ResponseEntity<?> getExpiredBatches(Principal principal) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        List<MedicineBatchResponse> result = this.medicineBatchService.getExpiredBatches();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/medicines/{medicineId}/batches")
    public ResponseEntity<?> getBatchesByMedicine(
            Principal principal,
            @PathVariable("medicineId") Long medicineId
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            List<MedicineBatchResponse> result = this.medicineBatchService.getBatchesByMedicineId(medicineId);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    private ResponseEntity<?> validatePharmacist(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        User user = this.userService.getUserByUsername(principal.getName());
        Employee employee = user != null ? this.employeeRepo.getEmployeeByUserId(user.getId()) : null;

        if (!hasPharmacistRole(user)
                || (employee != null && Boolean.FALSE.equals(employee.getActive()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Tài khoản hiện tại không phải dược sĩ"));
        }

        return null;
    }

    private boolean hasPharmacistRole(User user) {
        if (user == null || user.getRoleSet() == null) {
            return false;
        }

        return user.getRoleSet().stream()
                .map(Role::getCode)
                .filter(code -> code != null)
                .map(code -> code.trim().toUpperCase())
                .anyMatch(code -> "PHARMACIST".equals(code) || "ROLE_PHARMACIST".equals(code));
    }
}

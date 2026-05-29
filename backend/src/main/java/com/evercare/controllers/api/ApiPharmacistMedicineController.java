package com.evercare.controllers.api;

import com.evercare.dtos.request.MedicineRequest;
import com.evercare.dtos.request.MedicineStatusRequest;
import com.evercare.dtos.response.MedicineLowStockResponse;
import com.evercare.dtos.response.MedicineResponse;
import com.evercare.pojo.Employee;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.services.MedicineService;
import com.evercare.services.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacist/medicines")
@CrossOrigin
public class ApiPharmacistMedicineController {
    @Autowired
    private MedicineService medicineService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeRepository employeeRepo;

    @org.springframework.web.bind.annotation.GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockMedicines(Principal principal) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        List<MedicineLowStockResponse> result = this.medicineService.getLowStockMedicines();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(
            Principal principal,
            @RequestBody MedicineRequest request
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            MedicineResponse result = this.medicineService.createMedicine(request);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody MedicineRequest request
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            return ResponseEntity.ok(this.medicineService.updateMedicine(id, request));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody MedicineStatusRequest request
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            return ResponseEntity.ok(this.medicineService.updateStatus(id, request != null ? request.getActive() : null));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            this.medicineService.softDelete(id);
            return ResponseEntity.noContent().build();
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

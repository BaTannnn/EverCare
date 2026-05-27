package com.evercare.controllers.api;

import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.pojo.Employee;
import com.evercare.pojo.User;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.services.PrescriptionService;
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
    private UserService userService;

    @Autowired
    private EmployeeRepository employeeRepo;

    @GetMapping
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        List<PrescriptionResponse> result = this.prescriptionService.getPrescriptions(params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            return ResponseEntity.ok(this.prescriptionService.getPrescriptionById(id));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<?> dispense(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        ResponseEntity<?> authError = validatePharmacist(principal);
        if (authError != null) {
            return authError;
        }

        try {
            return ResponseEntity.ok(this.prescriptionService.dispensePrescription(principal.getName(), id));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }

    private ResponseEntity<?> validatePharmacist(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        User user = this.userService.getUserByUsername(principal.getName());
        Employee employee = this.employeeRepo.getEmployeeByUserId(user.getId());

        if (employee == null
                || Boolean.FALSE.equals(employee.getActive())
                || employee.getPosition() == null
                || !employee.getPosition().toLowerCase().contains("dược")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Tài khoản hiện tại không phải dược sĩ"));
        }

        return null;
    }
}

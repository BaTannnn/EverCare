package com.evercare.controllers.api;

import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.pojo.Employee;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.services.PrescriptionService;
import com.evercare.services.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

    @GetMapping
    public ResponseEntity<?> list(
            Principal principal,
            @RequestParam Map<String, String> params
    ) {
        validatePharmacist(principal);

        List<PrescriptionResponse> result = this.prescriptionService.getPrescriptions(params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        validatePharmacist(principal);
        return ResponseEntity.ok(this.prescriptionService.getPrescriptionById(id));
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<?> dispense(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        User user = validatePharmacist(principal);
        return ResponseEntity.ok(this.prescriptionService.dispensePrescription(user, id));
    }

    private User validatePharmacist(Principal principal) {
        if (principal == null) {
            throw new com.evercare.exceptions.AuthenticationRequiredException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(principal.getName());
        Employee employee = user != null ? user.getEmployee() : null;

        if (!hasPharmacistRole(user)
                || (employee != null && Boolean.FALSE.equals(employee.getActive()))) {
            throw new AccessDeniedException("Tài khoản hiện tại không phải dược sĩ");
        }

        return user;
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

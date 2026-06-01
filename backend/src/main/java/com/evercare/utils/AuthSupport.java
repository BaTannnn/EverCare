package com.evercare.utils;

import com.evercare.pojo.Doctor;
import com.evercare.pojo.Employee;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.repositories.PatientRepository;
import com.evercare.services.UserService;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthSupport {
    @Autowired
    private UserService userService;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        return getCurrentUser(username);
    }

    public User getCurrentUserAllowInactive() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        if (user == null) {
            throw new SecurityException("Vui lòng đăng nhập");
        }
        return user;
    }

    public User getCurrentUser(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản không hợp lệ");
        }

        return user;
    }

    public Patient getCurrentPatientOrNull() {
        try {
            return getCurrentPatientOrNull(getCurrentUser());
        } catch (SecurityException ex) {
            return null;
        }
    }

    public Patient getCurrentPatientOrNull(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }

        Patient patient = this.patientRepo.getPatientByUserId(currentUser.getId());
        return patient != null && !Boolean.FALSE.equals(patient.getActive()) ? patient : null;
    }

    public Patient requireCurrentPatient(String message) {
        Patient patient = getCurrentPatientOrNull(getCurrentUser());
        if (patient == null) {
            throw new NoSuchElementException(message);
        }
        return patient;
    }

    public Patient requireCurrentPatient(User currentUser, String message) {
        Patient patient = getCurrentPatientOrNull(currentUser);
        if (patient == null) {
            throw new NoSuchElementException(message);
        }
        return patient;
    }

    public Patient requireCurrentPatient(RuntimeException exception) {
        Patient patient = getCurrentPatientOrNull(getCurrentUser());
        if (patient == null) {
            throw exception;
        }
        return patient;
    }

    public User requireReceptionistUser() {
        User user = getCurrentUser();
        if (!hasAnyRole(user, "ROLE_RECEPTIONIST", "ROLE_ADMIN")) {
            throw new AccessDeniedException("Tài khoản không có quyền lễ tân");
        }
        return user;
    }

    public Doctor requireCurrentDoctor(String username) {
        User user = getCurrentUser(username);
        Doctor doctor = this.doctorRepo.getDoctorByUserId(user.getId());
        if (!hasRole(user, "DOCTOR")
                || doctor == null
                || Boolean.FALSE.equals(doctor.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải bác sĩ đang hoạt động");
        }
        return doctor;
    }

    public Employee requireCurrentEmployee(String username, String expectedRole, String message) {
        User user = getCurrentUser(username);
        Employee employee = this.employeeRepo.getEmployeeByUserId(user.getId());
        if (!hasRole(user, expectedRole)
                || employee == null
                || Boolean.FALSE.equals(employee.getActive())) {
            throw new SecurityException(message);
        }
        return employee;
    }

    public Employee getCurrentReceptionistEmployee(User user) {
        Employee employee = this.employeeRepo.getEmployeeByUserId(user.getId());
        if (employee == null && !hasAnyRole(user, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Tài khoản lễ tân chưa liên kết hồ sơ nhân viên");
        }
        return employee;
    }

    public boolean hasRole(User user, String expectedRole) {
        if (user == null || user.getRoleSet() == null || expectedRole == null) {
            return false;
        }

        String expected = normalizeRole(expectedRole);
        for (Role role : user.getRoleSet()) {
            if (role != null && role.getCode() != null && normalizeRole(role.getCode()).equals(expected)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyRole(User user, String... roles) {
        if (roles == null) {
            return false;
        }

        for (String role : roles) {
            if (hasRole(user, role)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
    }
}

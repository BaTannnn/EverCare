package com.evercare.services.impl;

import com.evercare.dtos.request.EmployeeRequest;
import com.evercare.pojo.Employee;
import com.evercare.pojo.User;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.repositories.UserRepository;
import com.evercare.services.EmployeeService;
import com.evercare.utils.AuthSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AuthSupport authSupport;
    @Override
    public List<Employee> getEmployees(Map<String, String> params) {
        return this.employeeRepo.getEmployees(params);
    }

    @Override
    public Employee getEmployeeById(int id) {
        return this.employeeRepo.getEmployeeById(id);
    }

    @Override
    public Employee createEmployee(EmployeeRequest req) {
        validateRequest(req, false);

        Employee employee = new Employee();
        applyRequest(employee, req, true);
        employee.setActive(true);
        employee.setCreatedAt(new Date());
        employee.setUpdatedAt(new Date());

        this.employeeRepo.addEmployee(employee);
        return employee;
    }

    @Override
    public Employee updateEmployee(int id, EmployeeRequest req) {
        validateRequest(req, true);

        Employee existing = this.employeeRepo.getEmployeeById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Nhân viên không tồn tại");
        }
        if (Boolean.FALSE.equals(existing.getActive())) {
            throw new IllegalArgumentException("Nhân viên đã bị xóa hoặc ngưng hoạt động");
        }

        applyRequest(existing, req, false);
        existing.setUpdatedAt(new Date());

        this.employeeRepo.updateEmployee(existing);
        return existing;
    }

    @Override
    public void softDelete(int id) {
        Employee existing = this.employeeRepo.getEmployeeById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Nhân viên không tồn tại");
        }

        existing.setActive(false);
        existing.setUpdatedAt(new Date());
        this.employeeRepo.updateEmployee(existing);
    }

    @Override
    public long getTotalPages(Map<String, String> params) {
        return this.employeeRepo.getTotalPages(params);
    }

    @Override
    public List<User> getSelectableUsers(Long currentUserId) {
        List<User> users = this.userRepo.getActiveUsers();
        List<User> selectable = new ArrayList<>();

        for (User user : users) {
            if (user == null) {
                continue;
            }

            if (currentUserId != null && currentUserId.equals(user.getId())) {
                selectable.add(user);
                continue;
            }

            if (user.getRoleSet() != null
                    && !user.getRoleSet().isEmpty()
                    && !this.authSupport.hasAnyRole(user, "DOCTOR", "PATIENT", "ADMIN")
                    && user.getEmployee() == null) {
                selectable.add(user);
            }
        }

        if (currentUserId != null && selectable.stream().noneMatch(u -> currentUserId.equals(u.getId()))) {
            User currentUser = this.userRepo.findById(currentUserId);
            if (currentUser != null && Boolean.TRUE.equals(currentUser.getActive())) {
                selectable.add(0, currentUser);
            }
        }

        return selectable;
    }

    private void applyRequest(Employee employee, EmployeeRequest req, boolean generateCode) {
        if (generateCode || employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()) {
            employee.setEmployeeCode(generateEmployeeCode());
        }

        employee.setFullName(normalizeText(req.getFullName(), "Họ tên nhân viên"));
        employee.setGender(normalizeNullableText(req.getGender()));
        employee.setDateOfBirth(parseDate(req.getDateOfBirth(), "Ngày sinh"));
        employee.setPhone(normalizeText(req.getPhone(), "Số điện thoại"));
        employee.setEmail(normalizeNullableText(req.getEmail()));
        employee.setAddress(normalizeNullableText(req.getAddress()));
        employee.setPosition(normalizeText(req.getPosition(), "Chức vụ"));
        employee.setSalary(req.getSalary() != null ? req.getSalary() : BigDecimal.ZERO);
        employee.setHiredDate(parseDate(req.getHiredDate(), "Ngày vào làm"));

        if (req.getUserId() == null) {
            return;
        }

        User user = this.userRepo.findById(req.getUserId());
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new IllegalArgumentException("Tài khoản liên kết không tồn tại hoặc đã ngưng hoạt động");
        }

        Employee linked = user.getEmployee() != null ? user.getEmployee() : this.employeeRepo.getEmployeeByUserId(user.getId());
        if (linked != null && (employee.getId() == null || !employee.getId().equals(linked.getId()))) {
            throw new IllegalArgumentException("Tài khoản này đã được gắn cho một nhân viên khác");
        }

        employee.setUserId(user);
    }

    private void validateRequest(EmployeeRequest req, boolean editing) {
        if (req == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ");
        }

        if (!editing && req.getUserId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn tài khoản liên kết");
        }
    }

    private String normalizeText(String value, String fieldName) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }

        return value.trim();
    }

    private String normalizeNullableText(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    private Date parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            synchronized (DATE_FORMAT) {
                return DATE_FORMAT.parse(value.trim());
            }
        } catch (ParseException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ");
        }
    }

    private String generateEmployeeCode() {
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        return "EMP_" + ts + "_" + suffix;
    }

}

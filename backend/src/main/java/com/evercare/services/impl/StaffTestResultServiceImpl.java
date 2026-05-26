package com.evercare.services.impl;

import com.evercare.dtos.request.TestResultRequest;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.mappers.TestResultMapper;
import com.evercare.pojo.Employee;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.User;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.MedicalRecordServiceRepository;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.repositories.TestResultRepository;
import com.evercare.services.StaffTestResultService;
import com.evercare.services.UserService;
import java.util.Date;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StaffTestResultServiceImpl implements StaffTestResultService {
    @Autowired
    private TestResultRepository testResultRepo;

    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

    @Autowired
    private MedicalServiceRepository medicalServiceRepo;

    @Autowired
    private MedicalRecordServiceRepository medicalRecordServiceRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private UserService userService;

    @Override
    public TestResultResponse createTestResult(String username, Long recordId, TestResultRequest request) {
        Employee employee = getCurrentEmployee(username);
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);

        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        MedicalService service = loadService(request);
        validateServiceWasOrdered(medicalRecord.getId(), service.getId());
        Date now = new Date();

        TestResult testResult = new TestResult();
        testResult.setResultCode(generateResultCode());
        testResult.setMedicalRecordId(medicalRecord);
        testResult.setServiceId(service);
        testResult.setPerformedBy(employee);
        testResult.setResultDate(now);
        testResult.setCreatedAt(now);
        testResult.setUpdatedAt(now);
        testResult.setActive(true);
        applyRequest(testResult, request, service);

        this.testResultRepo.addTestResult(testResult);

        return TestResultMapper.toResponse(testResult);
    }

    @Override
    public TestResultResponse updateTestResult(String username, Long id, TestResultRequest request) {
        Employee employee = getCurrentEmployee(username);
        TestResult testResult = this.testResultRepo.getTestResultById(id);

        if (testResult == null) {
            throw new NoSuchElementException("Không tìm thấy kết quả xét nghiệm");
        }

        MedicalService service = request != null && request.getServiceId() != null ? loadService(request) : testResult.getServiceId();
        if (service != null && testResult.getMedicalRecordId() != null) {
            validateServiceWasOrdered(testResult.getMedicalRecordId().getId(), service.getId());
        }
        applyRequest(testResult, request, service);
        testResult.setServiceId(service);
        testResult.setPerformedBy(employee);
        testResult.setUpdatedAt(new Date());

        this.testResultRepo.updateTestResult(testResult);

        return TestResultMapper.toResponse(testResult);
    }

    private MedicalService loadService(TestResultRequest request) {
        if (request == null || request.getServiceId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ");
        }

        MedicalService service = this.medicalServiceRepo.getServiceById(request.getServiceId().intValue());
        if (service == null || Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại hoặc đã ngưng hoạt động");
        }

        return service;
    }

    private void applyRequest(TestResult testResult, TestResultRequest request, MedicalService service) {
        if (request == null) {
            return;
        }

        if (request.getResultTitle() != null && !request.getResultTitle().isBlank()) {
            testResult.setResultTitle(request.getResultTitle().trim());
        } else if (testResult.getResultTitle() == null || testResult.getResultTitle().isBlank()) {
            testResult.setResultTitle(service != null ? service.getName() : "Kết quả xét nghiệm");
        }

        if (request.getResultContent() != null) {
            testResult.setResultContent(request.getResultContent().trim());
        }

        if (request.getFileUrl() != null) {
            testResult.setFileUrl(request.getFileUrl().trim());
        }

        if (request.getConclusion() != null) {
            testResult.setConclusion(request.getConclusion().trim());
        }
    }

    private void validateServiceWasOrdered(Long recordId, Long serviceId) {
        boolean ordered = this.medicalRecordServiceRepo
                .getServicesByMedicalRecordId(recordId)
                .stream()
                .anyMatch(s -> s.getServiceId() != null && serviceId.equals(s.getServiceId().getId()));

        if (!ordered) {
            throw new IllegalStateException("Dịch vụ này chưa được bác sĩ chỉ định trong bệnh án");
        }
    }

    private Employee getCurrentEmployee(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        Employee employee = this.employeeRepo.getEmployeeByUserId(user.getId());

        if (employee == null || Boolean.FALSE.equals(employee.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");
        }

        return employee;
    }

    private String generateResultCode() {
        return "TR" + System.currentTimeMillis();
    }
}

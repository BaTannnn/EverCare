package com.evercare.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evercare.dtos.request.TestResultRequest;
import com.evercare.dtos.response.AppointmentPatientResponse;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.StaffTestRequestDetailResponse;
import com.evercare.dtos.response.StaffTestRequestSummaryResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.exceptions.CloudinaryUploadException;
import com.evercare.mappers.MedicalRecordMapper;
import com.evercare.mappers.MedicalRecordServiceMapper;
import com.evercare.mappers.TestResultMapper;
import com.evercare.pojo.Employee;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.Patient;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.User;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.MedicalRecordServiceRepository;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.repositories.TestResultRepository;
import com.evercare.services.StaffTestResultService;
import com.evercare.utils.AuthSupport;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private Cloudinary cloudinary;
    @Autowired
    private AuthSupport authSupport;
    @Override
    public List<StaffTestRequestSummaryResponse> getPendingTestRequests(String username, Map<String, String> params) {
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");

        Map<Long, StaffTestRequestSummaryResponse> result = new LinkedHashMap<>();
        for (MedicalRecordService request : this.medicalRecordServiceRepo.getPendingTestRequests(params)) {
            MedicalRecord medicalRecord = request.getMedicalRecordId();
            if (medicalRecord == null) {
                continue;
            }

            StaffTestRequestSummaryResponse summary = result.computeIfAbsent(
                    medicalRecord.getId(),
                    id -> toSummaryResponse(medicalRecord)
            );
            summary.setPendingServiceCount(summary.getPendingServiceCount() + 1);
            summary.getPendingServices().add(MedicalRecordServiceMapper.toSummaryResponse(request, Collections.emptyList()));

            String requestedAt = format(request.getCreatedAt());
            if (summary.getRequestedAt() == null || requestedAt != null && requestedAt.compareTo(summary.getRequestedAt()) > 0) {
                summary.setRequestedAt(requestedAt);
            }
        }

        return result.values().stream().toList();
    }

    @Override
    public StaffTestRequestDetailResponse getTestRequestDetail(String username, Long recordId) {
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");

        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);
        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        List<MedicalRecordService> services = this.medicalRecordServiceRepo.getServicesByMedicalRecordId(recordId);
        Map<Long, List<TestResult>> resultsByServiceId = this.testResultRepo.getTestResultsByMedicalRecordId(recordId)
                .stream()
                .filter(r -> r.getServiceId() != null)
                .collect(Collectors.groupingBy(r -> r.getServiceId().getId()));

        List<MedicalRecordServiceResponse> serviceResponses = services.stream()
                .map(service -> MedicalRecordServiceMapper.toResponse(
                        service,
                        service.getServiceId() != null
                                ? resultsByServiceId.getOrDefault(service.getServiceId().getId(), Collections.emptyList())
                                : Collections.emptyList()
                ))
                .toList();

        StaffTestRequestDetailResponse response = new StaffTestRequestDetailResponse();
        response.setMedicalRecord(MedicalRecordMapper.toResponse(medicalRecord));
        response.setPatient(toPatientResponse(medicalRecord.getPatientId()));
        if (medicalRecord.getDoctorId() != null) {
            response.setDoctorId(medicalRecord.getDoctorId().getId());
            response.setDoctorName(medicalRecord.getDoctorId().getFullName());
        }
        response.setServices(serviceResponses);

        return response;
    }

    @Override
    public List<TestResultResponse> getTestResults(String username, Map<String, String> params) {
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");

        return this.testResultRepo.getTestResults(params)
                .stream()
                .map(TestResultMapper::toResponse)
                .toList();
    }

    @Override
    public TestResultResponse getTestResultById(String username, Long id) {
        this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");

        TestResult testResult = this.testResultRepo.getTestResultById(id);
        if (testResult == null) {
            throw new NoSuchElementException("Không tìm thấy kết quả xét nghiệm");
        }

        return TestResultMapper.toResponse(testResult);
    }

    @Override
    public TestResultResponse createTestResult(String username, Long recordId, TestResultRequest request) {
        Employee employee = this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);

        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        MedicalService service = loadService(request);
        validateServiceWasOrdered(medicalRecord.getId(), service.getId());
        validateResultNotDuplicated(medicalRecord.getId(), service.getId(), null);
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
        UploadedFile uploadedFile = applyRequest(testResult, request, service);

        try {
            this.testResultRepo.addTestResult(testResult);
        } catch (RuntimeException ex) {
            deleteUploadedFile(uploadedFile);
            throw ex;
        }

        return TestResultMapper.toResponse(testResult);
    }

    @Override
    public TestResultResponse updateTestResult(String username, Long id, TestResultRequest request) {
        Employee employee = this.authSupport.requireCurrentEmployee(username, "LAB_TECH", "Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");
        TestResult testResult = this.testResultRepo.getTestResultById(id);

        if (testResult == null) {
            throw new NoSuchElementException("Không tìm thấy kết quả xét nghiệm");
        }

        MedicalService service = request != null && request.getServiceId() != null ? loadService(request) : testResult.getServiceId();
        if (service != null && testResult.getMedicalRecordId() != null) {
            validateServiceWasOrdered(testResult.getMedicalRecordId().getId(), service.getId());
            validateResultNotDuplicated(testResult.getMedicalRecordId().getId(), service.getId(), testResult.getId());
        }
        UploadedFile uploadedFile = applyRequest(testResult, request, service);
        testResult.setServiceId(service);
        testResult.setPerformedBy(employee);
        testResult.setUpdatedAt(new Date());

        try {
            this.testResultRepo.updateTestResult(testResult);
        } catch (RuntimeException ex) {
            deleteUploadedFile(uploadedFile);
            throw ex;
        }

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

    private UploadedFile applyRequest(TestResult testResult, TestResultRequest request, MedicalService service) {
        if (request == null) {
            return null;
        }

        if (request.getResultTitle() != null && !request.getResultTitle().isBlank()) {
            testResult.setResultTitle(request.getResultTitle().trim());
        } else if (testResult.getResultTitle() == null || testResult.getResultTitle().isBlank()) {
            testResult.setResultTitle(service != null ? service.getName() : "Kết quả xét nghiệm");
        }

        if (request.getResultContent() != null) {
            testResult.setResultContent(request.getResultContent().trim());
        }

        UploadedFile uploadedFile = null;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            uploadedFile = uploadResultFile(request.getFile());
            testResult.setFileUrl(uploadedFile.secureUrl());
        }

        if (request.getConclusion() != null) {
            testResult.setConclusion(request.getConclusion().trim());
        }

        return uploadedFile;
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

    private void validateResultNotDuplicated(Long recordId, Long serviceId, Long excludeResultId) {
        if (this.testResultRepo.existsByMedicalRecordIdAndServiceId(recordId, serviceId, excludeResultId)) {
            throw new IllegalStateException("Dịch vụ này đã có kết quả xét nghiệm");
        }
    }

    private String generateResultCode() {
        return "TR" + System.currentTimeMillis();
    }

    private UploadedFile uploadResultFile(MultipartFile file) {
        validateResultFile(file);

        try {
            Map res = this.cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "evercare/test-results"
                    )
            );

            Object secureUrl = res.get("secure_url");
            if (secureUrl == null) {
                throw new CloudinaryUploadException("Cloudinary không trả về đường dẫn file");
            }

            Object publicId = res.get("public_id");
            return new UploadedFile(secureUrl.toString(), publicId != null ? publicId.toString() : null);
        } catch (CloudinaryUploadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudinaryUploadException("Không thể upload file kết quả lên Cloudinary", ex);
        }
    }

    private void validateResultFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File kết quả không được vượt quá 10MB");
        }

        String contentType = file.getContentType();
        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("File kết quả phải là PDF");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.trim().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("File kết quả phải có đuôi .pdf");
        }

        try {
            byte[] header = file.getInputStream().readNBytes(4);
            if (header.length < 4
                    || header[0] != '%'
                    || header[1] != 'P'
                    || header[2] != 'D'
                    || header[3] != 'F') {
                throw new IllegalArgumentException("Nội dung file không đúng định dạng PDF");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Không thể đọc file kết quả");
        }
    }

    private void deleteUploadedFile(UploadedFile uploadedFile) {
        if (uploadedFile == null || uploadedFile.publicId() == null || uploadedFile.publicId().isBlank()) {
            return;
        }

        try {
            this.cloudinary.uploader().destroy(
                    uploadedFile.publicId(),
                    ObjectUtils.asMap("resource_type", "raw")
            );
        } catch (Exception ignored) {
            // Best effort cleanup: DB transaction result is more important than cleanup failure here.
        }
    }

    private record UploadedFile(String secureUrl, String publicId) {
    }

    private StaffTestRequestSummaryResponse toSummaryResponse(MedicalRecord medicalRecord) {
        StaffTestRequestSummaryResponse response = new StaffTestRequestSummaryResponse();
        response.setMedicalRecordId(medicalRecord.getId());
        response.setRecordCode(medicalRecord.getRecordCode());
        response.setVisitDate(format(medicalRecord.getVisitDate()));
        response.setPendingServiceCount(0);

        if (medicalRecord.getPatientId() != null) {
            response.setPatientId(medicalRecord.getPatientId().getId());
            response.setPatientCode(medicalRecord.getPatientId().getPatientCode());
            response.setPatientName(medicalRecord.getPatientId().getFullName());
        }

        if (medicalRecord.getDoctorId() != null) {
            response.setDoctorId(medicalRecord.getDoctorId().getId());
            response.setDoctorName(medicalRecord.getDoctorId().getFullName());
        }

        return response;
    }

    private AppointmentPatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        AppointmentPatientResponse response = new AppointmentPatientResponse();
        response.setId(patient.getId());
        response.setPatientCode(patient.getPatientCode());
        response.setFullName(patient.getFullName());
        response.setGender(patient.getGender());
        response.setDateOfBirth(format(patient.getDateOfBirth()));
        response.setPhone(patient.getPhone());
        response.setEmail(patient.getEmail());
        return response;
    }

    private String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }
}

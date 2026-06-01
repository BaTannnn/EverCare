package com.evercare.services.impl;

import com.evercare.dtos.response.TestResultFileResponse;
import com.evercare.exceptions.FileProxyException;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.Employee;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Role;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.User;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.EmployeeRepository;
import com.evercare.repositories.PatientRepository;
import com.evercare.repositories.TestResultRepository;
import com.evercare.services.TestResultFileService;
import com.evercare.services.UserService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TestResultFileServiceImpl implements TestResultFileService {
    private static final int MAX_FILE_BYTES = 10 * 1024 * 1024;

    @Autowired
    private TestResultRepository testResultRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private UserService userService;

    @Override
    public TestResultFileResponse getFileForDoctor(String username, Long resultId) {
        User user = getCurrentUser(username);
        Doctor doctor = this.doctorRepo.getDoctorByUserId(user.getId());
        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải bác sĩ đang hoạt động");
        }

        TestResult testResult = loadTestResult(resultId);
        MedicalRecord medicalRecord = testResult.getMedicalRecordId();
        if (medicalRecord == null
                || medicalRecord.getDoctorId() == null
                || !doctor.getId().equals(medicalRecord.getDoctorId().getId())) {
            throw new SecurityException("Bác sĩ chỉ được xem file kết quả thuộc bệnh án mình phụ trách");
        }

        return fetchPdf(testResult);
    }

    @Override
    public TestResultFileResponse getFileForStaff(String username, Long resultId) {
        User user = getCurrentUser(username);
        Employee employee = this.employeeRepo.getEmployeeByUserId(user.getId());
        if (employee == null || Boolean.FALSE.equals(employee.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải nhân viên y tế đang hoạt động");
        }

        TestResult testResult = loadTestResult(resultId);
        boolean owner = testResult.getPerformedBy() != null && employee.getId().equals(testResult.getPerformedBy().getId());
        if (!owner && !hasRole(user, "LAB_TECH")) {
            throw new SecurityException("Nhân viên y tế không có quyền xem file kết quả này");
        }

        return fetchPdf(testResult);
    }

    @Override
    public TestResultFileResponse getFileForPatient(String username, Long resultId) {
        User user = getCurrentUser(username);
        Patient patient = this.patientRepo.getPatientByUserId(user.getId());
        if (patient == null || Boolean.FALSE.equals(patient.getActive())) {
            throw new SecurityException("Tài khoản hiện tại chưa có hồ sơ bệnh nhân");
        }

        TestResult testResult = loadTestResult(resultId);
        MedicalRecord medicalRecord = testResult.getMedicalRecordId();
        if (medicalRecord == null
                || medicalRecord.getPatientId() == null
                || !patient.getId().equals(medicalRecord.getPatientId().getId())) {
            throw new SecurityException("Bệnh nhân chỉ được xem file kết quả của chính mình");
        }

        return fetchPdf(testResult);
    }

    private TestResult loadTestResult(Long resultId) {
        TestResult testResult = this.testResultRepo.getTestResultById(resultId);
        if (testResult == null) {
            throw new NoSuchElementException("Không tìm thấy kết quả xét nghiệm");
        }

        if (testResult.getFileUrl() == null || testResult.getFileUrl().isBlank()) {
            throw new NoSuchElementException("Kết quả xét nghiệm chưa có file PDF");
        }

        return testResult;
    }

    private User getCurrentUser(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản không hợp lệ");
        }

        return user;
    }

    private TestResultFileResponse fetchPdf(TestResult testResult) {
        String fileUrl = testResult.getFileUrl().trim();
        URI uri = parseCloudinaryUri(fileUrl);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new FileProxyException("Không thể tải file kết quả từ Cloudinary");
            }

            byte[] content = readBounded(connection.getInputStream());
            validatePdfHeader(content);

            return new TestResultFileResponse(content, buildFilename(testResult));
        } catch (FileProxyException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FileProxyException("Không thể mở file kết quả", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URI parseCloudinaryUri(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || host == null
                    || !isCloudinaryHost(host)) {
                throw new FileProxyException("Đường dẫn file kết quả không hợp lệ");
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new FileProxyException("Đường dẫn file kết quả không hợp lệ", ex);
        }
    }

    private boolean isCloudinaryHost(String host) {
        String normalizedHost = host.toLowerCase();
        return "res.cloudinary.com".equals(normalizedHost)
                || normalizedHost.endsWith(".cloudinary.com");
    }

    private byte[] readBounded(InputStream inputStream) throws Exception {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                if (total > MAX_FILE_BYTES) {
                    throw new FileProxyException("File kết quả vượt quá giới hạn 10MB");
                }
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private void validatePdfHeader(byte[] content) {
        if (content == null
                || content.length < 4
                || content[0] != '%'
                || content[1] != 'P'
                || content[2] != 'D'
                || content[3] != 'F') {
            throw new FileProxyException("Nội dung file không đúng định dạng PDF");
        }
    }

    private String buildFilename(TestResult testResult) {
        String code = testResult.getResultCode() != null ? testResult.getResultCode() : "test-result-" + testResult.getId();
        return code.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf";
    }

    private boolean hasRole(User user, String expectedRole) {
        if (user == null || user.getRoleSet() == null) {
            return false;
        }

        String normalizedExpectedRole = expectedRole.toUpperCase();
        return user.getRoleSet().stream()
                .map(Role::getCode)
                .filter(code -> code != null)
                .map(code -> code.trim().toUpperCase())
                .anyMatch(code -> code.equals(normalizedExpectedRole) || code.equals("ROLE_" + normalizedExpectedRole));
    }
}

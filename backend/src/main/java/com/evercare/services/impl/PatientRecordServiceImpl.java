package com.evercare.services.impl;

import com.evercare.dtos.response.MedicalRecordDetailResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.dtos.response.PrescriptionResponse;
import com.evercare.dtos.response.TestResultResponse;
import com.evercare.mappers.MedicalRecordMapper;
import com.evercare.mappers.PrescriptionMapper;
import com.evercare.mappers.TestResultMapper;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Prescription;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.User;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.MedicalRecordServiceRepository;
import com.evercare.repositories.PatientRepository;
import com.evercare.repositories.PrescriptionRepository;
import com.evercare.repositories.TestResultRepository;
import com.evercare.services.PatientRecordService;
import com.evercare.services.UserService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientRecordServiceImpl implements PatientRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

    @Autowired
    private MedicalRecordServiceRepository medicalRecordServiceRepo;

    @Autowired
    private TestResultRepository testResultRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<MedicalRecordResponse> getMedicalRecordsByCurrentPatient(Map<String, String> params) {
        Patient patient = getCurrentPatientOrNull();
        if (patient == null) {
            return Collections.emptyList();
        }

        DateRange range = parseDateRange(params);

        return this.medicalRecordRepo.getMedicalRecordsByPatientId(patient.getId(), range.from(), range.to(), params)
                .stream()
                .map(MedicalRecordMapper::toResponse)
                .toList();
    }

    @Override
    public MedicalRecordDetailResponse getMedicalRecordByCurrentPatient(Long recordId) {
        Patient patient = requireCurrentPatient();
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordByPatientIdAndId(patient.getId(), recordId);
        if (medicalRecord == null) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        List<com.evercare.pojo.MedicalRecordService> services = this.medicalRecordServiceRepo
                .getServicesByMedicalRecordId(medicalRecord.getId());
        List<TestResult> testResults = this.testResultRepo.getTestResultsByMedicalRecordId(medicalRecord.getId());
        Prescription prescription = this.prescriptionRepo.getPrescriptionByMedicalRecordId(medicalRecord.getId());

        return MedicalRecordMapper.toDetailResponse(medicalRecord, services, testResults, prescription);
    }

    @Override
    public List<TestResultResponse> getTestResultsByCurrentPatient(Map<String, String> params) {
        Patient patient = getCurrentPatientOrNull();
        if (patient == null) {
            return Collections.emptyList();
        }

        DateRange range = parseDateRange(params);

        return this.testResultRepo.getTestResultsByPatientId(patient.getId(), range.from(), range.to(), params)
                .stream()
                .map(TestResultMapper::toResponse)
                .toList();
    }

    @Override
    public List<PrescriptionResponse> getPrescriptionsByCurrentPatient(Map<String, String> params) {
        Patient patient = getCurrentPatientOrNull();
        if (patient == null) {
            return Collections.emptyList();
        }

        DateRange range = parseDateRange(params);
        String status = params != null ? params.get("status") : null;

        List<PrescriptionResponse> result = this.prescriptionRepo
                .getPrescriptionsByPatientId(patient.getId(), status, range.from(), range.to(), params)
                .stream()
                .map(this::toPrescriptionSummaryResponse)
                .toList();

        return result;
    }

    @Override
    public PrescriptionResponse getPrescriptionByCurrentPatient(Long prescriptionId) {
        Patient patient = requireCurrentPatient();
        Prescription prescription = this.prescriptionRepo.getPrescriptionByPatientIdAndId(patient.getId(), prescriptionId);
        if (prescription == null) {
            throw new NoSuchElementException("Không tìm thấy đơn thuốc");
        }

        return PrescriptionMapper.toResponse(prescription, null);
    }

    private PrescriptionResponse toPrescriptionSummaryResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(prescription.getId());
        response.setPrescriptionCode(prescription.getPrescriptionCode());
        response.setPrescribedAt(prescription.getPrescribedAt() != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(prescription.getPrescribedAt())
                : null);
        response.setStatus(prescription.getStatus());
        response.setNote(prescription.getNote());
        response.setMedicalRecordId(prescription.getMedicalRecordId() != null ? prescription.getMedicalRecordId().getId() : null);
        response.setDoctorId(prescription.getDoctorId() != null ? prescription.getDoctorId().getId() : null);
        response.setDoctorName(prescription.getDoctorId() != null ? prescription.getDoctorId().getFullName() : null);
        response.setPatientId(prescription.getPatientId() != null ? prescription.getPatientId().getId() : null);
        response.setPatientCode(prescription.getPatientId() != null ? prescription.getPatientId().getPatientCode() : null);
        response.setPatientName(prescription.getPatientId() != null ? prescription.getPatientId().getFullName() : null);
        response.setItems(Collections.emptyList());
        return response;
    }

    private Patient getCurrentPatientOrNull() {
        User currentUser = getCurrentUser();
        Patient patient = this.patientRepo.getPatientByUserId(currentUser.getId());
        if (patient == null || Boolean.FALSE.equals(patient.getActive())) {
            return null;
        }
        return patient;
    }

    private Patient requireCurrentPatient() {
        Patient patient = getCurrentPatientOrNull();
        if (patient == null || Boolean.FALSE.equals(patient.getActive())) {
            throw new NoSuchElementException("Bạn chưa tạo hồ sơ bệnh nhân");
        }
        return patient;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        if (user == null || Boolean.FALSE.equals(user.getActive())) {
            throw new SecurityException("Tài khoản không hợp lệ");
        }

        return user;
    }

    private DateRange parseDateRange(Map<String, String> params) {
        LocalDate from = parseDate(params != null ? params.get("from") : null);
        LocalDate to = parseDate(params != null ? params.get("to") : null);
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("from không được lớn hơn to");
        }
        return new DateRange(from, to);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Ngày không hợp lệ");
        }
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}

package com.evercare.services.impl;

import com.evercare.dtos.request.MedicalRecordServiceRequest;
import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordServiceResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.mappers.MedicalRecordServiceMapper;
import com.evercare.mappers.MedicalRecordMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.MedicalRecordService;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.TestResult;
import com.evercare.pojo.User;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.repositories.MedicalRecordServiceRepository;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.repositories.TestResultRepository;
import com.evercare.services.DoctorMedicalRecordService;
import com.evercare.services.UserService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorMedicalRecordServiceImpl implements DoctorMedicalRecordService {
    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

    @Autowired
    private MedicalRecordServiceRepository medicalRecordServiceRepo;

    @Autowired
    private MedicalServiceRepository medicalServiceRepo;

    @Autowired
    private TestResultRepository testResultRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private UserService userService;

    @Override
    public MedicalRecordResponse updateMedicalRecord(String username, Long recordId, UpdateMedicalRecordRequest request) {
        Doctor doctor = getCurrentDoctor(username);
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);

        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        validateOwnedMedicalRecord(doctor, medicalRecord);
        validateAppointmentNotCompleted(medicalRecord);
        applyRequest(medicalRecord, request);
        medicalRecord.setUpdatedAt(new java.util.Date());

        this.medicalRecordRepo.updateMedicalRecord(medicalRecord);

        return MedicalRecordMapper.toResponse(medicalRecord);
    }

    @Override
    public MedicalRecordServiceResponse addService(String username, Long recordId, MedicalRecordServiceRequest request) {
        Doctor doctor = getCurrentDoctor(username);
        MedicalRecord medicalRecord = loadEditableMedicalRecordForDoctor(doctor, recordId);

        if (request == null || request.getServiceId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ");
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0");
        }

        MedicalService service = this.medicalServiceRepo.getServiceById(request.getServiceId().intValue());
        if (service == null || Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalArgumentException("Dịch vụ không tồn tại hoặc đã ngưng hoạt động");
        }

        Date now = new Date();
        MedicalRecordService recordService = new MedicalRecordService();
        recordService.setMedicalRecordId(medicalRecord);
        recordService.setServiceId(service);
        recordService.setQuantity(quantity);
        recordService.setUnitPrice(service.getPrice());
        recordService.setResultSummary(request.getResultSummary() != null ? request.getResultSummary().trim() : null);
        recordService.setCreatedAt(now);
        recordService.setUpdatedAt(now);
        recordService.setActive(true);

        this.medicalRecordServiceRepo.addMedicalRecordService(recordService);

        return MedicalRecordServiceMapper.toResponse(recordService, Collections.emptyList());
    }

    @Override
    public List<MedicalRecordServiceResponse> getServices(String username, Long recordId) {
        Doctor doctor = getCurrentDoctor(username);
        MedicalRecord medicalRecord = loadMedicalRecordForDoctor(doctor, recordId);
        List<MedicalRecordService> services = this.medicalRecordServiceRepo.getServicesByMedicalRecordId(medicalRecord.getId());
        Map<Long, List<TestResult>> resultsByServiceId = this.testResultRepo
                .getTestResultsByMedicalRecordId(medicalRecord.getId())
                .stream()
                .filter(r -> r.getServiceId() != null)
                .collect(Collectors.groupingBy(r -> r.getServiceId().getId()));

        return services.stream()
                .map(s -> MedicalRecordServiceMapper.toResponse(
                        s,
                        s.getServiceId() != null
                                ? resultsByServiceId.getOrDefault(s.getServiceId().getId(), Collections.emptyList())
                                : Collections.emptyList()
                ))
                .toList();
    }

    private void validateOwnedMedicalRecord(Doctor doctor, MedicalRecord medicalRecord) {
        if (medicalRecord.getDoctorId() == null
                || medicalRecord.getPatientId() == null
                || medicalRecord.getAppointmentId() == null) {
            throw new IllegalStateException("Bệnh án thiếu thông tin bác sĩ, bệnh nhân hoặc lịch hẹn");
        }

        if (!doctor.getId().equals(medicalRecord.getDoctorId().getId())) {
            throw new SecurityException("Chỉ bác sĩ phụ trách bệnh án mới được cập nhật");
        }
    }

    private void validateAppointmentNotCompleted(MedicalRecord medicalRecord) {
        Appointment appointment = medicalRecord.getAppointmentId();
        if (AppointmentStatus.COMPLETED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Không thể sửa bệnh án khi lịch khám đã COMPLETED");
        }

        if (appointment.getMedicalRecord() != null
                && !medicalRecord.getId().equals(appointment.getMedicalRecord().getId())) {
            throw new IllegalStateException("Một lịch hẹn chỉ được gắn với một bệnh án");
        }
    }

    private MedicalRecord loadEditableMedicalRecordForDoctor(Doctor doctor, Long recordId) {
        MedicalRecord medicalRecord = loadMedicalRecordForDoctor(doctor, recordId);
        Appointment appointment = medicalRecord.getAppointmentId();

        if (!AppointmentStatus.IN_PROGRESS.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể chỉ định dịch vụ khi lịch khám đang IN_PROGRESS");
        }

        return medicalRecord;
    }

    private MedicalRecord loadMedicalRecordForDoctor(Doctor doctor, Long recordId) {
        MedicalRecord medicalRecord = this.medicalRecordRepo.getMedicalRecordById(recordId);

        if (medicalRecord == null || Boolean.FALSE.equals(medicalRecord.getActive())) {
            throw new NoSuchElementException("Không tìm thấy bệnh án");
        }

        validateOwnedMedicalRecord(doctor, medicalRecord);

        return medicalRecord;
    }

    private void applyRequest(MedicalRecord medicalRecord, UpdateMedicalRecordRequest request) {
        if (request == null) {
            return;
        }

        if (request.getChiefComplaint() != null) {
            medicalRecord.setChiefComplaint(request.getChiefComplaint().trim());
        }

        if (request.getDiagnosis() != null) {
            medicalRecord.setDiagnosis(request.getDiagnosis().trim());
        }

        if (request.getTreatmentPlan() != null) {
            medicalRecord.setTreatmentPlan(request.getTreatmentPlan().trim());
        }

        if (request.getDoctorNote() != null) {
            medicalRecord.setDoctorNote(request.getDoctorNote().trim());
        }
    }

    private Doctor getCurrentDoctor(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        Doctor doctor = this.doctorRepo.getDoctorByUserId(user.getId());

        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải bác sĩ đang hoạt động");
        }

        return doctor;
    }
}

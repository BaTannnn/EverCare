package com.evercare.services.impl;

import com.evercare.dtos.request.UpdateMedicalRecordRequest;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.mappers.MedicalRecordMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.User;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.services.DoctorMedicalRecordService;
import com.evercare.services.UserService;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorMedicalRecordServiceImpl implements DoctorMedicalRecordService {
    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

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

        validateCanUpdate(doctor, medicalRecord);
        applyRequest(medicalRecord, request);
        medicalRecord.setUpdatedAt(new java.util.Date());

        this.medicalRecordRepo.updateMedicalRecord(medicalRecord);

        return MedicalRecordMapper.toResponse(medicalRecord);
    }

    private void validateCanUpdate(Doctor doctor, MedicalRecord medicalRecord) {
        if (medicalRecord.getDoctorId() == null
                || medicalRecord.getPatientId() == null
                || medicalRecord.getAppointmentId() == null) {
            throw new IllegalStateException("Bệnh án thiếu thông tin bác sĩ, bệnh nhân hoặc lịch hẹn");
        }

        if (!doctor.getId().equals(medicalRecord.getDoctorId().getId())) {
            throw new SecurityException("Chỉ bác sĩ phụ trách bệnh án mới được cập nhật");
        }

        Appointment appointment = medicalRecord.getAppointmentId();
        if (AppointmentStatus.COMPLETED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Không thể sửa bệnh án khi lịch khám đã COMPLETED");
        }

        if (appointment.getMedicalRecord() != null
                && !medicalRecord.getId().equals(appointment.getMedicalRecord().getId())) {
            throw new IllegalStateException("Một lịch hẹn chỉ được gắn với một bệnh án");
        }
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

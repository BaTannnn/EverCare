package com.evercare.services.impl;

import com.evercare.dtos.request.StartExaminationRequest;
import com.evercare.dtos.response.DoctorAppointmentResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.enums.InvoiceStatus;
import com.evercare.mappers.AppointmentMapper;
import com.evercare.mappers.MedicalRecordMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalRecord;
import com.evercare.pojo.Role;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.MedicalRecordRepository;
import com.evercare.services.DoctorAppointmentService;
import com.evercare.services.UserService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorAppointmentServiceImpl implements DoctorAppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private MedicalRecordRepository medicalRecordRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<DoctorAppointmentResponse> getAppointmentsByDate(String username, LocalDate date) {
        Doctor doctor = getCurrentDoctor(username);

        return this.appointmentRepo
                .getAppointmentsByDoctorAndDate(doctor.getId(), Date.valueOf(date))
                .stream()
                .map(AppointmentMapper::toDoctorSummaryResponse)
                .toList();
    }

    @Override
    public DoctorAppointmentResponse getAppointmentById(String username, Long appointmentId) {
        Doctor doctor = getCurrentDoctor(username);
        Appointment appointment = this.appointmentRepo.getAppointmentByDoctorAndId(doctor.getId(), appointmentId);

        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        return AppointmentMapper.toDoctorResponse(appointment);
    }

    @Override
    public MedicalRecordResponse startExamination(String username, Long appointmentId, StartExaminationRequest request) {
        Doctor doctor = getCurrentDoctor(username);
        Appointment appointment = this.appointmentRepo.getAppointmentById(appointmentId);

        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        if (appointment.getDoctorId() == null || !doctor.getId().equals(appointment.getDoctorId().getId())) {
            throw new SecurityException("Lịch hẹn không thuộc bác sĩ đang đăng nhập");
        }

        if (!AppointmentStatus.canStartExamination(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể bắt đầu khám với lịch hẹn đã check-in và đang WAITING");
        }

        java.util.Date now = new java.util.Date();
        appointment.setStatus(AppointmentStatus.IN_PROGRESS.getCode());
        appointment.setUpdatedAt(now);

        MedicalRecord medicalRecord = appointment.getMedicalRecord();
        if (medicalRecord == null) {
            medicalRecord = new MedicalRecord();
            medicalRecord.setRecordCode(generateRecordCode(appointment.getId()));
            medicalRecord.setAppointmentId(appointment);
            medicalRecord.setDoctorId(doctor);
            medicalRecord.setPatientId(appointment.getPatientId());
            medicalRecord.setVisitDate(now);
            medicalRecord.setPaymentStatus(InvoiceStatus.UNPAID.getCode());
            medicalRecord.setCreatedAt(now);
            medicalRecord.setActive(true);
            appointment.setMedicalRecord(medicalRecord);
        }

        medicalRecord.setUpdatedAt(now);
        applyStartExaminationRequest(medicalRecord, request);

        if (medicalRecord.getId() == null) {
            this.medicalRecordRepo.addMedicalRecord(medicalRecord);
        } else {
            this.medicalRecordRepo.updateMedicalRecord(medicalRecord);
        }

        this.appointmentRepo.updateAppointment(appointment);

        return MedicalRecordMapper.toResponse(medicalRecord);
    }

    private Doctor getCurrentDoctor(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityException("Vui lòng đăng nhập");
        }

        User user = this.userService.getUserByUsername(username);
        Doctor doctor = this.doctorRepo.getDoctorByUserId(user.getId());

        if (!hasRole(user, "DOCTOR")
                || doctor == null
                || Boolean.FALSE.equals(doctor.getActive())) {
            throw new SecurityException("Tài khoản hiện tại không phải bác sĩ đang hoạt động");
        }

        return doctor;
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

    private void applyStartExaminationRequest(MedicalRecord medicalRecord, StartExaminationRequest request) {
        if (request == null) {
            return;
        }

        if (request.getChiefComplaint() != null) {
            medicalRecord.setChiefComplaint(request.getChiefComplaint().trim());
        }

        if (request.getInitialNote() != null) {
            medicalRecord.setDoctorNote(request.getInitialNote().trim());
        }
    }

    private String generateRecordCode(Long appointmentId) {
        return String.format("MR%012d", appointmentId);
    }
}

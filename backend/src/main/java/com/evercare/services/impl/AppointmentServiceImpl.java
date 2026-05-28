package com.evercare.services.impl;

import com.evercare.dtos.request.AppointmentCancelRequest;
import com.evercare.dtos.request.AppointmentRequest;
import com.evercare.dtos.response.AppointmentResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.enums.DoctorWorkStatus;
import com.evercare.mappers.AppointmentMapper;
import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.DoctorSchedule;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.Notification;
import com.evercare.pojo.Patient;
import com.evercare.pojo.User;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.repositories.DoctorScheduleRepository;
import com.evercare.repositories.DoctorRepository;
import com.evercare.repositories.MedicalServiceRepository;
import com.evercare.repositories.NotificationRepository;
import com.evercare.repositories.PatientRepository;
import com.evercare.services.AppointmentService;
import com.evercare.services.UserService;
import java.math.BigInteger;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private static final String TYPE_APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER";
    private static final String STATUS_BOOKED = AppointmentStatus.BOOKED.getCode();
    private static final String STATUS_CANCELLED = AppointmentStatus.CANCELLED.getCode();
    private static final String STATUS_WAITING = AppointmentStatus.WAITING.getCode();
    private static final String STATUS_IN_PROGRESS = AppointmentStatus.IN_PROGRESS.getCode();
    private static final String STATUS_COMPLETED = AppointmentStatus.COMPLETED.getCode();
    private static final int DEFAULT_APPOINTMENT_MINUTES = 30;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private DoctorScheduleRepository scheduleRepo;

    @Autowired
    private MedicalServiceRepository serviceRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đặt lịch không hợp lệ");
        }

        User currentUser = getCurrentUser();
        Patient currentPatient = requireCurrentPatient();
        Doctor doctor = loadValidDoctor(request.getDoctorId());
        MedicalService service = loadValidService(request.getServiceId());

        if (doctor.getDepartmentId() != null && service.getDepartmentId() != null
                && doctor.getDepartmentId().getId() != null
                && service.getDepartmentId().getId() != null
                && !doctor.getDepartmentId().getId().equals(service.getDepartmentId().getId())) {
            throw new IllegalStateException("Dịch vụ không thuộc chuyên khoa của bác sĩ");
        }

        if (request.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày khám");
        }

        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ khám");
        }

        java.time.LocalDate appointmentDate = request.getAppointmentDate();
        java.time.LocalTime startTime = request.getStartTime();
        java.time.LocalTime endTime = request.getEndTime();
        if (endTime == null) {
            endTime = startTime.plusMinutes(DEFAULT_APPOINTMENT_MINUTES);
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Giờ kết thúc phải lớn hơn giờ bắt đầu");
        }

        if (appointmentDate.isBefore(java.time.LocalDate.now())
                || (appointmentDate.isEqual(java.time.LocalDate.now()) && startTime.isBefore(java.time.LocalTime.now()))) {
            throw new IllegalStateException("Không thể đặt lịch trong quá khứ");
        }

        DoctorSchedule schedule = this.scheduleRepo.getScheduleCoveringAppointmentTime(
                doctor.getId(),
                appointmentDate,
                startTime,
                endTime
        );

        if (schedule == null) {
            throw new IllegalStateException("Bác sĩ không có lịch trống trong khung giờ này.");
        }

        if (this.appointmentRepo.existsAppointmentByDoctorAndTime(
                doctor.getId(),
                java.sql.Date.valueOf(appointmentDate),
                Time.valueOf(startTime),
                null
        )) {
            throw new IllegalStateException("Khung giờ này đã có người đặt.");
        }

        long bookedCount = this.appointmentRepo.countBookedAppointmentsByDoctorAndDateAndWindow(
                doctor.getId(),
                java.sql.Date.valueOf(appointmentDate),
                Time.valueOf(schedule.getStartTime()),
                Time.valueOf(schedule.getEndTime())
        );

        if (schedule.getMaxPatients() != null && bookedCount >= schedule.getMaxPatients()) {
            throw new IllegalStateException("Khung giờ này đã đủ số lượng bệnh nhân.");
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode(generateAppointmentCode());
        appointment.setAppointmentDate(java.sql.Date.valueOf(appointmentDate));
        appointment.setStartTime(Time.valueOf(startTime));
        appointment.setEndTime(Time.valueOf(endTime));
        appointment.setReason(trimToNull(request.getReason()));
        appointment.setSymptomNote(trimToNull(request.getSymptomNote()));
        appointment.setStatus(STATUS_BOOKED);
        appointment.setPatientId(currentPatient);
        appointment.setDoctorId(doctor);
        appointment.setServiceId(service);
        appointment.setCreatedBy(currentUser);
        appointment.setActive(true);
        Date now = new Date();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        Appointment saved = this.appointmentRepo.createAppointment(appointment);
        createNotification(currentUser, "Đặt lịch khám thành công", "Bạn đã đặt lịch khám thành công.", saved.getId());

        return AppointmentMapper.toPatientResponse(saved);
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByCurrentPatient(Map<String, String> params) {
        Patient currentPatient = getCurrentPatientOrNull();
        if (currentPatient == null) {
            return new ArrayList<>();
        }

        List<AppointmentResponse> result = new ArrayList<>();
        for (Appointment appointment : this.appointmentRepo.getAppointmentsByPatientId(currentPatient.getId(), params)) {
            result.add(AppointmentMapper.toPatientResponse(appointment));
        }
        return result;
    }

    @Override
    public AppointmentResponse getAppointmentByCurrentPatient(Long appointmentId) {
        Patient currentPatient = requireCurrentPatient();
        Appointment appointment = this.appointmentRepo.getAppointmentByPatientIdAndId(currentPatient.getId(), appointmentId);
        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }
        return AppointmentMapper.toPatientResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, AppointmentCancelRequest request) {
        User currentUser = getCurrentUser();
        Patient currentPatient = requireCurrentPatient();
        Appointment appointment = this.appointmentRepo.getAppointmentByPatientIdAndId(currentPatient.getId(), appointmentId);

        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        if (AppointmentStatus.CANCELLED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Lịch hẹn đã được hủy");
        }

        if (AppointmentStatus.IN_PROGRESS.getCode().equalsIgnoreCase(appointment.getStatus())
                || AppointmentStatus.COMPLETED.getCode().equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Không thể hủy lịch khi đã bắt đầu khám");
        }

        if (!(AppointmentStatus.BOOKED.getCode().equalsIgnoreCase(appointment.getStatus())
                || AppointmentStatus.WAITING.getCode().equalsIgnoreCase(appointment.getStatus()))) {
            throw new IllegalStateException("Chỉ có thể hủy lịch ở trạng thái BOOKED hoặc WAITING");
        }

        appointment.setStatus(STATUS_CANCELLED);
        appointment.setCancelReason(request != null ? trimToNull(request.getReason()) : null);
        appointment.setUpdatedAt(new Date());
        this.appointmentRepo.updateAppointment(appointment);

        createNotification(currentUser, "Đã hủy lịch khám", "Lịch khám của bạn đã được hủy.", appointment.getId());
        return AppointmentMapper.toPatientResponse(appointment);
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

    private Patient getCurrentPatientOrNull() {
        try {
            User user = getCurrentUser();
            Patient patient = this.patientRepo.getPatientByUserId(user.getId());
            if (patient == null || Boolean.FALSE.equals(patient.getActive())) {
                return null;
            }
            return patient;
        } catch (SecurityException ex) {
            return null;
        }
    }

    private Patient requireCurrentPatient() {
        Patient patient = getCurrentPatientOrNull();
        if (patient == null) {
            throw new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch khám.");
        }
        return patient;
    }

    private Doctor loadValidDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Vui lòng chọn bác sĩ");
        }

        Doctor doctor = this.doctorRepo.getDoctorById(doctorId.intValue());
        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())) {
            throw new IllegalStateException("Bác sĩ không tồn tại hoặc đã ngưng hoạt động");
        }

        if (DoctorWorkStatus.INACTIVE.getCode().equalsIgnoreCase(doctor.getWorkStatus())) {
            throw new IllegalStateException("Bác sĩ không còn làm việc");
        }

        return doctor;
    }

    private MedicalService loadValidService(Long serviceId) {
        if (serviceId == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ");
        }

        MedicalService service = this.serviceRepo.getServiceById(serviceId.intValue());
        if (service == null || Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalStateException("Dịch vụ không tồn tại hoặc đã ngưng hoạt động");
        }

        return service;
    }

    private void createNotification(User user, String title, String content, Long relatedId) {
        if (this.notificationRepo == null || user == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(TYPE_APPOINTMENT_REMINDER);
        if (relatedId != null) {
            notification.setRelatedId(BigInteger.valueOf(relatedId));
        }
        notification.setActive(true);
        notification.setCreatedAt(new Date());
        this.notificationRepo.createNotification(notification);
    }

    private String generateAppointmentCode() {
        return "APT_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "_" + String.format("%06d", Math.abs(UUID.randomUUID().hashCode()) % 1_000_000);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

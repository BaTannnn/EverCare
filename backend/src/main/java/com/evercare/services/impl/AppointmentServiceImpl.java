package com.evercare.services.impl;

import com.evercare.dtos.request.AppointmentCancelRequest;
import com.evercare.dtos.request.AppointmentRequest;
import com.evercare.dtos.request.CheckInRequest;
import com.evercare.dtos.request.PatientRequest;
import com.evercare.dtos.response.AppointmentCancelResponse;
import com.evercare.dtos.response.AppointmentResponse;
import com.evercare.enums.AppointmentStatus;
import com.evercare.enums.MedicalServiceType;
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
import com.evercare.repositories.NotificationRepository;
import com.evercare.repositories.PatientRepository;
import com.evercare.services.AppointmentService;
import com.evercare.utils.AuthSupport;
import com.evercare.utils.LookupSupport;
import java.math.BigInteger;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

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
    private DoctorScheduleRepository scheduleRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private AuthSupport authSupport;

    @Autowired
    private LookupSupport lookupSupport;

    @Override
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        validateBookingRequest(request);
        User currentUser = this.authSupport.getCurrentUser();
        Patient currentPatient = this.authSupport.requireCurrentPatient(new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch khám."));
        Doctor doctor = this.lookupSupport.requireWorkingDoctor(request.getDoctorId());
        MedicalService service = this.lookupSupport.requireActiveExaminationService(request.getServiceId());

        validateDepartmentMatch(null, doctor, service);
        LocalDate appointmentDate = requireAppointmentDate(request.getAppointmentDate());
        LocalTime startTime = requireStartTime(request.getStartTime());
        LocalTime endTime = resolveAppointmentEndTime(startTime, request.getEndTime());
        validateDateTime(appointmentDate, startTime, endTime);
        validateDoctorScheduleAndCapacity(doctor.getId(), appointmentDate, startTime, endTime, null);

        Appointment saved = createAndPersistAppointment(
                currentUser,
                currentPatient,
                doctor,
                service,
                appointmentDate,
                startTime,
                endTime,
                STATUS_BOOKED,
                request.getReason(),
                request.getSymptomNote()
        );
        createNotification(currentUser, "Đặt lịch khám thành công", "Bạn đã đặt lịch khám thành công.", saved.getId());

        return AppointmentMapper.toPatientResponse(saved);
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByCurrentPatient(Map<String, String> params) {
        Patient currentPatient = this.authSupport.getCurrentPatientOrNull();
        if (currentPatient == null) {
            return List.of();
        }

        return toPatientResponses(this.appointmentRepo.getAppointmentsByPatientId(currentPatient.getId(), params));
    }

    @Override
    public AppointmentResponse getAppointmentByCurrentPatient(Long appointmentId) {
        Patient currentPatient = this.authSupport.requireCurrentPatient(new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch khám."));
        Appointment appointment = this.appointmentRepo.getAppointmentByPatientIdAndId(currentPatient.getId(), appointmentId);
        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }
        return AppointmentMapper.toPatientResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentCancelResponse cancelAppointment(Long appointmentId, AppointmentCancelRequest request) {
        User currentUser = this.authSupport.getCurrentUser();
        Patient currentPatient = this.authSupport.requireCurrentPatient(new IllegalStateException("Bạn cần tạo hồ sơ bệnh nhân trước khi đặt lịch khám."));
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

        AppointmentCancelResponse response = new AppointmentCancelResponse();
        response.setAppointment(AppointmentMapper.toPatientResponse(appointment));

        try {
            createNotification(currentUser, "Đã hủy lịch khám", "Lịch khám của bạn đã được hủy.", appointment.getId(), TYPE_APPOINTMENT_REMINDER);
        } catch (RuntimeException ex) {
            logger.error("Cancel appointment notification failed for appointmentId={}", appointmentId, ex);
        }

        return response;
    }

    @Override
    public List<AppointmentResponse> getAppointmentsForReceptionist(Map<String, String> params) {
        this.authSupport.requireReceptionistUser();
        return toReceptionistResponses(this.appointmentRepo.getAppointmentsForReceptionist(params));
    }

    @Override
    public AppointmentResponse getAppointmentForReceptionist(Long appointmentId) {
        this.authSupport.requireReceptionistUser();

        Appointment appointment = this.appointmentRepo.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        return AppointmentMapper.toReceptionistResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointmentForReceptionist(AppointmentRequest request) {
        User currentUser = this.authSupport.requireReceptionistUser();
        validateBookingRequest(request);

        Patient patient = resolveReceptionistPatient(request);
        Doctor doctor = this.lookupSupport.requireWorkingDoctor(request.getDoctorId());
        MedicalService service = this.lookupSupport.requireActiveExaminationService(request.getServiceId());
        validateDepartmentMatch(request.getDepartmentId(), doctor, service);

        LocalDate appointmentDate = requireAppointmentDate(request.getAppointmentDate());
        LocalTime startTime = requireStartTime(request.getStartTime());
        LocalTime endTime = resolveAppointmentEndTime(startTime, request.getEndTime());

        validateDateTime(appointmentDate, startTime, endTime);
        validateDoctorScheduleAndCapacity(doctor.getId(), appointmentDate, startTime, endTime, null);

        boolean checkInNow = Boolean.TRUE.equals(request.getCheckInNow()) && appointmentDate.equals(LocalDate.now());
        String status = checkInNow ? STATUS_WAITING : STATUS_BOOKED;
        Appointment saved = createAndPersistAppointment(
                currentUser,
                patient,
                doctor,
                service,
                appointmentDate,
                startTime,
                endTime,
                status,
                request.getReason(),
                request.getSymptomNote()
        );
        notifyPatientByAppointment(
                patient,
                saved,
                checkInNow ? "Bạn đã được tiếp nhận" : "Đặt lịch khám thành công",
                checkInNow
                        ? "Bạn đã được tiếp nhận tại quầy và đang chờ bác sĩ khám."
                        : "Bạn đã đặt lịch khám thành công."
        );

        return AppointmentMapper.toReceptionistResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentForReceptionist(Long appointmentId, AppointmentRequest request) {
        this.authSupport.requireReceptionistUser();
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu cập nhật không hợp lệ");
        }

        Appointment appointment = this.appointmentRepo.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        if (!(STATUS_BOOKED.equalsIgnoreCase(appointment.getStatus())
                || STATUS_WAITING.equalsIgnoreCase(appointment.getStatus()))) {
            throw new IllegalStateException("Chỉ có thể cập nhật lịch hẹn đang ở trạng thái BOOKED hoặc WAITING");
        }

        boolean scheduleChangeRequested = request.getDepartmentId() != null
                || request.getDoctorId() != null
                || request.getServiceId() != null
                || request.getAppointmentDate() != null
                || request.getStartTime() != null
                || request.getEndTime() != null;

        Doctor doctor = request.getDoctorId() != null ? this.lookupSupport.requireWorkingDoctor(request.getDoctorId()) : appointment.getDoctorId();
        MedicalService service = request.getServiceId() != null ? this.lookupSupport.requireActiveExaminationService(request.getServiceId()) : appointment.getServiceId();

        if (scheduleChangeRequested) {
            validateExaminationService(service);
            validateDepartmentMatch(request.getDepartmentId(), doctor, service);

            LocalDate appointmentDate = request.getAppointmentDate() != null
                    ? request.getAppointmentDate()
                    : toLocalDate(appointment.getAppointmentDate());
            LocalTime startTime = request.getStartTime() != null
                    ? request.getStartTime()
                    : toLocalTime(appointment.getStartTime());
            LocalTime endTime = request.getEndTime() != null
                    ? request.getEndTime()
                    : (appointment.getEndTime() != null ? toLocalTime(appointment.getEndTime()) : startTime.plusMinutes(DEFAULT_APPOINTMENT_MINUTES));

            validateDateTime(appointmentDate, startTime, endTime);
            validateDoctorScheduleAndCapacity(doctor.getId(), appointmentDate, startTime, endTime, appointment.getId());

            appointment.setAppointmentDate(java.sql.Date.valueOf(appointmentDate));
            appointment.setStartTime(Time.valueOf(startTime));
            appointment.setEndTime(Time.valueOf(endTime));
            appointment.setDoctorId(doctor);
            appointment.setServiceId(service);
        }

        if (request.getReason() != null) {
            appointment.setReason(trimToNull(request.getReason()));
        }
        if (request.getSymptomNote() != null) {
            appointment.setSymptomNote(trimToNull(request.getSymptomNote()));
        }

        appointment.setUpdatedAt(new Date());
        this.appointmentRepo.updateAppointment(appointment);

        notifyPatientByAppointment(
                appointment.getPatientId(),
                appointment,
                "Lịch khám đã được cập nhật",
                "Thông tin lịch khám của bạn đã được lễ tân cập nhật."
        );

        return AppointmentMapper.toReceptionistResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse checkInAppointment(Long appointmentId, CheckInRequest request) {
        this.authSupport.requireReceptionistUser();

        Appointment appointment = this.appointmentRepo.getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new NoSuchElementException("Không tìm thấy lịch hẹn");
        }

        LocalDate appointmentDate = toLocalDate(appointment.getAppointmentDate());
        if (!LocalDate.now().equals(appointmentDate)) {
            throw new IllegalStateException("Chỉ có thể check-in lịch hẹn trong ngày hôm nay");
        }

        if (STATUS_WAITING.equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Bệnh nhân đã được tiếp nhận.");
        }

        if (!STATUS_BOOKED.equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Chỉ có thể tiếp nhận lịch hẹn đang ở trạng thái BOOKED.");
        }

        Patient patient = appointment.getPatientId();
        if (patient == null || Boolean.FALSE.equals(patient.getActive())) {
            throw new IllegalStateException("Bệnh nhân không còn hoạt động");
        }

        if (isBlank(patient.getFullName()) || isBlank(patient.getPhone())) {
            throw new IllegalStateException("Hồ sơ bệnh nhân thiếu thông tin bắt buộc: fullName, phone");
        }

        Doctor doctor = appointment.getDoctorId();
        if (doctor == null || Boolean.FALSE.equals(doctor.getActive())
                || DoctorWorkStatus.INACTIVE.getCode().equalsIgnoreCase(doctor.getWorkStatus())) {
            throw new IllegalStateException("Bác sĩ không tồn tại hoặc đã ngưng hoạt động");
        }

        MedicalService service = appointment.getServiceId();
        if (service != null && Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalStateException("Dịch vụ không tồn tại hoặc đã ngưng hoạt động");
        }

        appointment.setStatus(STATUS_WAITING);
        appointment.setUpdatedAt(new Date());
        this.appointmentRepo.updateAppointment(appointment);

        notifyPatientByAppointment(
                patient,
                appointment,
                "Bạn đã được tiếp nhận",
                "Bạn đã được tiếp nhận tại quầy và đang chờ bác sĩ khám."
        );

        return AppointmentMapper.toReceptionistResponse(appointment);
    }

    private void validateBookingRequest(AppointmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu đặt lịch không hợp lệ");
        }
    }

    private List<AppointmentResponse> toPatientResponses(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return List.of();
        }

        return appointments.stream()
                .map(AppointmentMapper::toPatientResponse)
                .toList();
    }

    private List<AppointmentResponse> toReceptionistResponses(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return List.of();
        }

        return appointments.stream()
                .map(AppointmentMapper::toReceptionistResponse)
                .toList();
    }

    private Appointment createAndPersistAppointment(
            User createdBy,
            Patient patient,
            Doctor doctor,
            MedicalService service,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime,
            String status,
            String reason,
            String symptomNote
    ) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentCode(generateAppointmentCode());
        appointment.setAppointmentDate(java.sql.Date.valueOf(appointmentDate));
        appointment.setStartTime(Time.valueOf(startTime));
        appointment.setEndTime(Time.valueOf(endTime));
        appointment.setReason(trimToNull(reason));
        appointment.setSymptomNote(trimToNull(symptomNote));
        appointment.setStatus(status);
        appointment.setPatientId(patient);
        appointment.setDoctorId(doctor);
        appointment.setServiceId(service);
        appointment.setCreatedBy(createdBy);
        appointment.setActive(true);

        Date now = new Date();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);

        return this.appointmentRepo.createAppointment(appointment);
    }

    private Patient resolveReceptionistPatient(AppointmentRequest request) {
        if (request.getPatientId() != null) {
            Patient patient = this.patientRepo.getPatientById(request.getPatientId());
            if (patient == null) {
                throw new NoSuchElementException("Không tìm thấy bệnh nhân");
            }
            if (Boolean.FALSE.equals(patient.getActive())) {
                throw new IllegalStateException("Bệnh nhân đang ngưng hoạt động");
            }
            return patient;
        }

        PatientRequest patientRequest = request.getPatient();
        String phone = patientRequest != null ? trimToNull(patientRequest.getPhone()) : null;
        String citizenId = patientRequest != null ? trimToNull(patientRequest.getCitizenId()) : null;

        Patient patientByPhone = phone != null ? this.patientRepo.getPatientByPhone(phone) : null;
        Patient patientByCitizenId = citizenId != null ? this.patientRepo.getPatientByCitizenId(citizenId) : null;

        if (patientByPhone != null && patientByCitizenId != null && !patientByPhone.getId().equals(patientByCitizenId.getId())) {
            throw new IllegalStateException("Thông tin định danh bệnh nhân không khớp");
        }

        Patient existingPatient = patientByPhone != null ? patientByPhone : patientByCitizenId;
        if (existingPatient != null) {
            if (Boolean.FALSE.equals(existingPatient.getActive())) {
                throw new IllegalStateException("Bệnh nhân đang ngưng hoạt động");
            }

            if (phone != null && !phone.equals(existingPatient.getPhone())) {
                throw new IllegalStateException("Thông tin định danh bệnh nhân không khớp");
            }

            if (citizenId != null && !citizenId.equals(trimToNull(existingPatient.getCitizenId()))) {
                throw new IllegalStateException("Thông tin định danh bệnh nhân không khớp");
            }

            return existingPatient;
        }

        if (patientRequest == null) {
            throw new IllegalArgumentException("Vui lòng cung cấp thông tin bệnh nhân");
        }

        String fullName = trimToNull(patientRequest.getFullName());
        String gender = trimToNull(patientRequest.getGender());
        String dateOfBirth = trimToNull(patientRequest.getDateOfBirth());

        if (isBlank(fullName) || isBlank(phone) || isBlank(gender) || isBlank(dateOfBirth)) {
            throw new IllegalArgumentException("Bệnh nhân tối thiểu phải có fullName, phone, gender, dateOfBirth");
        }

        Patient patient = new Patient();
        patient.setPatientCode(generatePatientCode());
        patient.setFullName(fullName);
        patient.setPhone(phone);
        patient.setEmail(trimToNull(patientRequest.getEmail()));
        patient.setGender(gender);
        patient.setDateOfBirth(java.sql.Date.valueOf(LocalDate.parse(dateOfBirth)));
        patient.setCitizenId(citizenId);
        patient.setHealthInsuranceNo(trimToNull(patientRequest.getHealthInsuranceNo()));
        patient.setAddress(trimToNull(patientRequest.getAddress()));
        patient.setEmergencyContactName(trimToNull(patientRequest.getEmergencyContactName()));
        patient.setEmergencyContactPhone(trimToNull(patientRequest.getEmergencyContactPhone()));
        patient.setBloodType(trimToNull(patientRequest.getBloodType()));
        patient.setAllergyNote(trimToNull(patientRequest.getAllergyNote()));
        patient.setMedicalHistoryNote(trimToNull(patientRequest.getMedicalHistoryNote()));
        patient.setActive(true);

        Date now = new Date();
        patient.setCreatedAt(now);
        patient.setUpdatedAt(now);

        return this.patientRepo.save(patient);
    }

    private void validateDepartmentMatch(Long departmentId, Doctor doctor, MedicalService service) {
        Long doctorDepartmentId = doctor != null && doctor.getDepartmentId() != null ? doctor.getDepartmentId().getId() : null;
        Long serviceDepartmentId = service != null && service.getDepartmentId() != null ? service.getDepartmentId().getId() : null;

        if (departmentId != null) {
            if (doctorDepartmentId == null || !departmentId.equals(doctorDepartmentId)) {
                throw new IllegalArgumentException("Bác sĩ không thuộc chuyên khoa được chọn");
            }

            if (serviceDepartmentId != null && !departmentId.equals(serviceDepartmentId)) {
                throw new IllegalArgumentException("Dịch vụ không thuộc chuyên khoa được chọn");
            }
        } else if (doctorDepartmentId != null && serviceDepartmentId != null && !doctorDepartmentId.equals(serviceDepartmentId)) {
            throw new IllegalArgumentException("Dịch vụ không thuộc chuyên khoa của bác sĩ");
        }
    }

    private void validateDoctorScheduleAndCapacity(Long doctorId,
                                                   LocalDate appointmentDate,
                                                   LocalTime startTime,
                                                   LocalTime endTime,
                                                   Long excludeAppointmentId) {
        DoctorSchedule schedule = this.scheduleRepo.getScheduleCoveringAppointmentTime(
                doctorId,
                appointmentDate,
                startTime,
                endTime
        );

        if (schedule == null) {
            throw new IllegalStateException("Bác sĩ không có lịch trống trong khung giờ này.");
        }

        long bookedCount = this.appointmentRepo.countBookedAppointmentsByDoctorAndDateAndWindow(
                doctorId,
                java.sql.Date.valueOf(appointmentDate),
                Time.valueOf(schedule.getStartTime()),
                Time.valueOf(schedule.getEndTime()),
                excludeAppointmentId
        );

        if (schedule.getMaxPatients() != null && bookedCount >= schedule.getMaxPatients()) {
            throw new IllegalStateException("Khung giờ này đã đủ số lượng bệnh nhân.");
        }
    }

    private void validateDateTime(LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {
        if (appointmentDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày khám");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ khám");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ kết thúc");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Giờ kết thúc phải lớn hơn giờ bắt đầu");
        }

        LocalDate today = LocalDate.now();
        if (appointmentDate.isBefore(today)
                || (appointmentDate.isEqual(today) && startTime.isBefore(LocalTime.now()))) {
            throw new IllegalStateException("Không thể đặt lịch trong quá khứ");
        }
    }

    private LocalTime resolveAppointmentEndTime(LocalTime startTime, LocalTime endTime) {
        return endTime != null ? endTime : startTime.plusMinutes(DEFAULT_APPOINTMENT_MINUTES);
    }

    private LocalDate requireAppointmentDate(LocalDate appointmentDate) {
        if (appointmentDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày khám");
        }
        return appointmentDate;
    }

    private LocalTime requireStartTime(LocalTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Vui lòng chọn giờ khám");
        }
        return startTime;
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date time) {
        if (time == null) {
            return null;
        }
        if (time instanceof java.sql.Time) {
            return ((java.sql.Time) time).toLocalTime();
        }
        return time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private void notifyPatientByAppointment(Patient patient, Appointment appointment, String title, String content) {
        if (patient == null || patient.getUserId() == null || Boolean.FALSE.equals(patient.getUserId().getActive())) {
            return;
        }

        createNotification(patient.getUserId(), title, content, appointment.getId(), TYPE_APPOINTMENT_REMINDER);
    }

    private String generatePatientCode() {
        return "PAT_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "_" + String.format("%06d", Math.abs(UUID.randomUUID().hashCode()) % 1_000_000);
    }

    private void validateExaminationService(MedicalService service) {
        if (service == null) {
            throw new IllegalArgumentException("Dịch vụ được chọn không phải dịch vụ khám. Vui lòng chọn dịch vụ có loại EXAMINATION.");
        }

        if (Boolean.FALSE.equals(service.getActive())) {
            throw new IllegalStateException("Dịch vụ không khả dụng");
        }

        if (!MedicalServiceType.EXAMINATION.getCode().equalsIgnoreCase(service.getServiceType())) {
            throw new IllegalArgumentException("Dịch vụ được chọn không phải dịch vụ khám. Vui lòng chọn dịch vụ có loại EXAMINATION.");
        }
    }

    private void createNotification(User user, String title, String content, Long relatedId, String notificationType) {
        try {
            if (this.notificationRepo == null || user == null) {
                return;
            }

            Notification notification = new Notification();
            notification.setUserId(user);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setNotificationType(notificationType);
            if (relatedId != null) {
                notification.setRelatedId(BigInteger.valueOf(relatedId));
            }
            notification.setActive(true);
            notification.setCreatedAt(new Date());
            this.notificationRepo.createNotification(notification);
        } catch (Exception ex) {
            // Notification is best-effort only.
        }
    }

    private void createNotification(User user, String title, String content, Long relatedId) {
        createNotification(user, title, content, relatedId, TYPE_APPOINTMENT_REMINDER);
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

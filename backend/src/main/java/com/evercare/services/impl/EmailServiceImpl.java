package com.evercare.services.impl;

import com.evercare.pojo.Appointment;
import com.evercare.pojo.Doctor;
import com.evercare.pojo.MedicalService;
import com.evercare.pojo.Patient;
import com.evercare.repositories.AppointmentRepository;
import com.evercare.services.EmailService;
import com.evercare.utils.DateTimeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String APPOINTMENT_CONFIRMATION_SUBJECT = "Xác nhận lịch hẹn khám tại EverCare";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private Environment env;

    @Override
    @Async("mailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendAppointmentConfirmationEmailAsync(Long appointmentId) {
        try {
            if (appointmentId == null) {
                return;
            }

            Appointment appointment = this.appointmentRepo.getAppointmentForEmail(appointmentId);
            if (appointment == null) {
                logger.warn("Skip appointment confirmation email: appointmentId={} not found", appointmentId);
                return;
            }

            Patient patient = appointment.getPatientId();
            String patientEmail = patient != null ? trimToNull(patient.getEmail()) : null;
            if (patientEmail == null) {
                logger.info("Skip appointment confirmation email: appointmentId={} has no patient email", appointmentId);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            String mailFrom = trimToNull(this.env.getProperty("mail.from"));
            if (mailFrom != null) {
                message.setFrom(mailFrom);
            }
            message.setTo(patientEmail);
            message.setSubject(APPOINTMENT_CONFIRMATION_SUBJECT);
            message.setText(buildAppointmentConfirmationBody(appointment));

            this.mailSender.send(message);
            logger.info("Appointment confirmation email sent for appointmentId={} to={}", appointmentId, patientEmail);
        } catch (Exception ex) {
            logger.error("Failed to send appointment confirmation email for appointmentId={}", appointmentId, ex);
        }
    }

    private String buildAppointmentConfirmationBody(Appointment appointment) {
        Patient patient = appointment.getPatientId();
        Doctor doctor = appointment.getDoctorId();
        MedicalService service = appointment.getServiceId();

        String patientName = patient != null ? defaultText(patient.getFullName(), "anh/chị") : "anh/chị";
        String appointmentCode = defaultText(appointment.getAppointmentCode(), "N/A");
        String serviceName = service != null ? defaultText(service.getName(), "N/A") : "N/A";
        String doctorName = doctor != null ? defaultText(doctor.getFullName(), "N/A") : "N/A";
        String appointmentDate = formatDate(appointment);
        String appointmentTime = formatTime(appointment);

        return "Xin chào " + patientName + ",\n\n"
                + "EverCare xác nhận lịch hẹn khám của anh/chị đã được đặt thành công.\n\n"
                + "Thông tin lịch hẹn:\n"
                + "- Mã lịch hẹn: " + appointmentCode + "\n"
                + "- Dịch vụ: " + serviceName + "\n"
                + "- Bác sĩ: " + doctorName + "\n"
                + "- Ngày khám: " + appointmentDate + "\n"
                + "- Giờ khám: " + appointmentTime + "\n"
                + "- Trạng thái: Đã đặt lịch\n\n"
                + "Vui lòng đến trước giờ hẹn khoảng 10–15 phút để làm thủ tục check-in.\n\n"
                + "Trân trọng,\n"
                + "EverCare Clinic";
    }

    private String formatDate(Appointment appointment) {
        LocalDate date = DateTimeUtils.toLocalDate(appointment.getAppointmentDate());
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String formatTime(Appointment appointment) {
        LocalTime startTime = DateTimeUtils.toLocalTime(appointment.getStartTime());
        return startTime != null ? startTime.format(TIME_FORMATTER) : "N/A";
    }

    private String defaultText(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed != null ? trimmed : fallback;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

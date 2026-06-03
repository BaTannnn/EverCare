package com.evercare.repositories;

import com.evercare.pojo.Appointment;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.sql.Time;
import java.time.LocalDate;

public interface AppointmentRepository {
    List<Appointment> getAppointmentsForReceptionist(Map<String, String> params);
    long countAppointmentsForReceptionist(Map<String, String> params);
    List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate);
    Map<String, Long> countAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate);
    long countBookedAppointmentsByDoctorAndDateAndWindow(Long doctorId, Date appointmentDate, Time startTime, Time endTime);
    long countBookedAppointmentsByDoctorAndDateAndWindow(Long doctorId, Date appointmentDate, Time startTime, Time endTime, Long excludeId);
    List<Appointment> getBookableAppointmentsByDoctorAndDateRange(Long doctorId, Date fromDate, Date toDate);
    List<Appointment> getAppointmentsByPatientId(Long patientId, Map<String, String> params);
    Appointment getAppointmentByPatientIdAndId(Long patientId, Long appointmentId);
    Appointment getAppointmentByPatientDoctorAndSlot(Long patientId, Long doctorId, Date appointmentDate, Time startTime, Time endTime);
    List<Appointment> getAppointmentsEligibleForNoShow(LocalDate currentDate);
    Appointment createAppointment(Appointment appointment);

    Appointment getAppointmentById(Long appointmentId);

    Appointment getAppointmentByDoctorAndId(Long doctorId, Long appointmentId);

    void updateAppointment(Appointment appointment);
}

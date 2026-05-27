package com.evercare.repositories;

import com.evercare.pojo.Appointment;
import java.util.Date;
import java.util.List;
import java.sql.Time;

public interface AppointmentRepository {
    List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate);
    long countBookedAppointmentsByDoctorAndDateAndWindow(Long doctorId, Date appointmentDate, Time startTime, Time endTime);

    Appointment getAppointmentById(Long appointmentId);

    Appointment getAppointmentByDoctorAndId(Long doctorId, Long appointmentId);

    void updateAppointment(Appointment appointment);
}

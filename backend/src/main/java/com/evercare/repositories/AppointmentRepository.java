package com.evercare.repositories;

import com.evercare.pojo.Appointment;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AppointmentRepository {
    List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate);

    Map<String, Long> countAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate);

    Appointment getAppointmentById(Long appointmentId);

    Appointment getAppointmentByDoctorAndId(Long doctorId, Long appointmentId);

    void updateAppointment(Appointment appointment);
}

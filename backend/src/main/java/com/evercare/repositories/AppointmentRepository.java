package com.evercare.repositories;

import com.evercare.pojo.Appointment;
import java.util.Date;
import java.util.List;

public interface AppointmentRepository {
    List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, Date appointmentDate);

    Appointment getAppointmentByDoctorAndId(Long doctorId, Long appointmentId);
}

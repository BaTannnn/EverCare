package com.evercare.services;

import com.evercare.dtos.response.DoctorAppointmentResponse;
import java.time.LocalDate;
import java.util.List;

public interface DoctorAppointmentService {
    List<DoctorAppointmentResponse> getAppointmentsByDate(String username, LocalDate date);

    DoctorAppointmentResponse getAppointmentById(String username, Long appointmentId);
}

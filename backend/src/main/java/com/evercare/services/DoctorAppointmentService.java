package com.evercare.services;

import com.evercare.dtos.request.StartExaminationRequest;
import com.evercare.dtos.response.DoctorAppointmentResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import java.time.LocalDate;
import java.util.List;

public interface DoctorAppointmentService {
    List<DoctorAppointmentResponse> getAppointmentsByDate(String username, LocalDate date);

    DoctorAppointmentResponse getAppointmentById(String username, Long appointmentId);

    MedicalRecordResponse startExamination(String username, Long appointmentId, StartExaminationRequest request);
}

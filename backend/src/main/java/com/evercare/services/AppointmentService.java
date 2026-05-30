package com.evercare.services;

import com.evercare.dtos.request.AppointmentCancelRequest;
import com.evercare.dtos.request.AppointmentRequest;
import com.evercare.dtos.response.AppointmentCancelResponse;
import com.evercare.dtos.response.AppointmentResponse;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentRequest request);
    List<AppointmentResponse> getAppointmentsByCurrentPatient(Map<String, String> params);
    AppointmentResponse getAppointmentByCurrentPatient(Long appointmentId);
    AppointmentCancelResponse cancelAppointment(Long appointmentId, AppointmentCancelRequest request);
}

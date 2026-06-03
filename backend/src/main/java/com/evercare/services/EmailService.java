package com.evercare.services;

public interface EmailService {
    void sendAppointmentConfirmationEmailAsync(Long appointmentId);
}

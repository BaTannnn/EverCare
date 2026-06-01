package com.evercare.dtos.response;

public class AppointmentCancelResponse {
    private AppointmentResponse appointment;

    public AppointmentResponse getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentResponse appointment) {
        this.appointment = appointment;
    }
}

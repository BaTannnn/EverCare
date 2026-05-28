package com.evercare.dtos.response;

public class AppointmentCancelResponse {
    private AppointmentResponse appointment;
    private RefundResponse refund;

    public AppointmentResponse getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentResponse appointment) {
        this.appointment = appointment;
    }

    public RefundResponse getRefund() {
        return refund;
    }

    public void setRefund(RefundResponse refund) {
        this.refund = refund;
    }
}

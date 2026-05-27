package com.evercare.dtos.response;

public class DoctorDashboardSummaryResponse {
    private Long todayAppointments;
    private Long waitingAppointments;
    private Long inProgressAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;

    public Long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(Long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public Long getWaitingAppointments() {
        return waitingAppointments;
    }

    public void setWaitingAppointments(Long waitingAppointments) {
        this.waitingAppointments = waitingAppointments;
    }

    public Long getInProgressAppointments() {
        return inProgressAppointments;
    }

    public void setInProgressAppointments(Long inProgressAppointments) {
        this.inProgressAppointments = inProgressAppointments;
    }

    public Long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(Long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public Long getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(Long cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }
}

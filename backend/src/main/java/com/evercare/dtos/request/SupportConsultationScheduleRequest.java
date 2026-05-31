package com.evercare.dtos.request;

public class SupportConsultationScheduleRequest {
    private Long doctorId;
    private String scheduledStart;
    private String scheduledEnd;
    private String meetLink;
    private String note;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(String scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public String getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(String scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

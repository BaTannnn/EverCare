package com.evercare.dtos.response;

public class DoctorAppointmentResponse {
    private Long id;
    private String appointmentCode;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private String reason;
    private String symptomNote;
    private String status;
    private String statusLabel;
    private AppointmentPatientResponse patient;
    private MedicalServiceResponse service;
    private MedicalRecordResponse medicalRecord;
    private PrescriptionResponse prescription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSymptomNote() {
        return symptomNote;
    }

    public void setSymptomNote(String symptomNote) {
        this.symptomNote = symptomNote;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public AppointmentPatientResponse getPatient() {
        return patient;
    }

    public void setPatient(AppointmentPatientResponse patient) {
        this.patient = patient;
    }

    public MedicalServiceResponse getService() {
        return service;
    }

    public void setService(MedicalServiceResponse service) {
        this.service = service;
    }

    public MedicalRecordResponse getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecordResponse medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public PrescriptionResponse getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionResponse prescription) {
        this.prescription = prescription;
    }
}

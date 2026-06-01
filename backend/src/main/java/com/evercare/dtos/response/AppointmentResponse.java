package com.evercare.dtos.response;

public class AppointmentResponse {
    private Long id;
    private String appointmentCode;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private String status;
    private String statusLabel;
    private String reason;
    private String symptomNote;
    private String cancelReason;
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private Long serviceId;
    private String serviceName;
    private PatientResponse patient;
    private DoctorResponse doctor;
    private MedicalServiceResponse service;

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

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public PatientResponse getPatient() {
        return patient;
    }

    public void setPatient(PatientResponse patient) {
        this.patient = patient;
    }

    public DoctorResponse getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorResponse doctor) {
        this.doctor = doctor;
    }

    public MedicalServiceResponse getService() {
        return service;
    }

    public void setService(MedicalServiceResponse service) {
        this.service = service;
    }
}

package com.evercare.dtos.response;

import java.util.List;

public class StaffTestRequestDetailResponse {
    private MedicalRecordResponse medicalRecord;
    private AppointmentPatientResponse patient;
    private Long doctorId;
    private String doctorName;
    private List<MedicalRecordServiceResponse> services;

    public MedicalRecordResponse getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecordResponse medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public AppointmentPatientResponse getPatient() {
        return patient;
    }

    public void setPatient(AppointmentPatientResponse patient) {
        this.patient = patient;
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

    public List<MedicalRecordServiceResponse> getServices() {
        return services;
    }

    public void setServices(List<MedicalRecordServiceResponse> services) {
        this.services = services;
    }
}

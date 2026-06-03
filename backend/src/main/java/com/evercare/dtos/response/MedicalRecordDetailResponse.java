package com.evercare.dtos.response;

import java.util.List;

public class MedicalRecordDetailResponse extends MedicalRecordResponse {
    private String appointmentCode;
    private String doctorName;
    private String departmentName;
    private AppointmentResponse appointment;
    private AppointmentPatientResponse patient;
    private DoctorResponse doctor;
    private DepartmentResponse department;
    private List<MedicalRecordServiceResponse> services;
    private List<TestResultResponse> testResults;
    private PrescriptionResponse prescription;
    private InvoiceResponse invoice;

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
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

    public AppointmentResponse getAppointment() {
        return appointment;
    }

    public void setAppointment(AppointmentResponse appointment) {
        this.appointment = appointment;
    }

    public AppointmentPatientResponse getPatient() {
        return patient;
    }

    public void setPatient(AppointmentPatientResponse patient) {
        this.patient = patient;
    }

    public DoctorResponse getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorResponse doctor) {
        this.doctor = doctor;
    }

    public DepartmentResponse getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentResponse department) {
        this.department = department;
    }

    public List<MedicalRecordServiceResponse> getServices() {
        return services;
    }

    public void setServices(List<MedicalRecordServiceResponse> services) {
        this.services = services;
    }

    public List<TestResultResponse> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<TestResultResponse> testResults) {
        this.testResults = testResults;
    }

    public PrescriptionResponse getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionResponse prescription) {
        this.prescription = prescription;
    }

    public InvoiceResponse getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceResponse invoice) {
        this.invoice = invoice;
    }
}

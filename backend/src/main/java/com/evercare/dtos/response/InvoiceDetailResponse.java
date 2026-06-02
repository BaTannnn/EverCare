package com.evercare.dtos.response;

import java.util.List;

public class InvoiceDetailResponse extends InvoiceResponse {
    private AppointmentPatientResponse patient;
    private MedicalRecordResponse medicalRecord;
    private List<PaymentResponse> payments;

    public AppointmentPatientResponse getPatient() {
        return patient;
    }

    public void setPatient(AppointmentPatientResponse patient) {
        this.patient = patient;
    }

    public MedicalRecordResponse getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecordResponse medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public List<PaymentResponse> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentResponse> payments) {
        this.payments = payments;
    }
}

package com.evercare.dtos.response;

import java.util.List;

public class InvoiceDetailResponse extends InvoiceResponse {
    private PatientResponse patient;
    private MedicalRecordResponse medicalRecord;
    private List<PaymentResponse> payments;

    public PatientResponse getPatient() {
        return patient;
    }

    public void setPatient(PatientResponse patient) {
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

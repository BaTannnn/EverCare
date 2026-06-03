package com.evercare.dtos.response.statistics;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RevenueDetailResponse {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private Long invoiceId;
    private String invoiceCode;
    private String patientName;
    private String medicalRecordCode;
    private BigDecimal totalAmount;
    private String status;
    private String createdAt;

    public RevenueDetailResponse() {
    }

    public RevenueDetailResponse(
            Long invoiceId,
            String invoiceCode,
            String patientName,
            String medicalRecordCode,
            BigDecimal totalAmount,
            String status,
            Date createdAt
    ) {
        this.invoiceId = invoiceId;
        this.invoiceCode = invoiceCode;
        this.patientName = patientName;
        this.medicalRecordCode = medicalRecordCode;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = format(createdAt);
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getMedicalRecordCode() {
        return medicalRecordCode;
    }

    public void setMedicalRecordCode(String medicalRecordCode) {
        this.medicalRecordCode = medicalRecordCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    private String format(Date value) {
        if (value == null) {
            return null;
        }

        return new SimpleDateFormat(DATETIME_PATTERN).format(value);
    }
}

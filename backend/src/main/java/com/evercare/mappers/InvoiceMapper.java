package com.evercare.mappers;

import com.evercare.dtos.response.AppointmentPatientResponse;
import com.evercare.dtos.response.InvoiceDetailResponse;
import com.evercare.dtos.response.InvoiceResponse;
import com.evercare.dtos.response.MedicalRecordResponse;
import com.evercare.dtos.response.PaymentResponse;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Patient;
import com.evercare.pojo.Payment;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public final class InvoiceMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private InvoiceMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        return toResponse(invoice, BigDecimal.ZERO);
    }

    public static InvoiceResponse toResponse(Invoice invoice, BigDecimal totalTestAmount) {
        if (invoice == null) {
            return null;
        }

        InvoiceResponse res = new InvoiceResponse();
        fillBase(invoice, res, totalTestAmount, true, false);
        return res;
    }

    public static InvoiceResponse toReceptionistResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceResponse res = new InvoiceResponse();
        fillBase(invoice, res, BigDecimal.ZERO, true, true);
        return res;
    }

    public static InvoiceResponse toEmbeddedResponse(Invoice invoice, BigDecimal totalTestAmount) {
        if (invoice == null) {
            return null;
        }

        InvoiceResponse res = new InvoiceResponse();
        fillBase(invoice, res, totalTestAmount, false, false);
        return res;
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice, List<Payment> payments) {
        return toDetailResponse(invoice, payments, BigDecimal.ZERO);
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice, List<Payment> payments, BigDecimal totalTestAmount) {
        if (invoice == null) {
            return null;
        }

        InvoiceDetailResponse res = new InvoiceDetailResponse();
        fillBase(invoice, res, totalTestAmount, true, true);
        res.setPatient(toPatientResponse(invoice.getPatientId()));
        res.setMedicalRecord(invoice.getMedicalRecordId() != null ? MedicalRecordMapper.toResponse(invoice.getMedicalRecordId()) : null);
        List<PaymentResponse> paymentResponses = payments == null
                ? Collections.emptyList()
                : payments.stream().map(PaymentMapper::toResponse).toList();
        res.setPayments(paymentResponses);
        return res;
    }

    private static void fillBase(
            Invoice invoice,
            InvoiceResponse res,
            BigDecimal totalTestAmount,
            boolean includeMedicalRecord,
            boolean includePatient
    ) {
        res.setId(invoice.getId());
        res.setInvoiceCode(invoice.getInvoiceCode());
        res.setTotalServiceAmount(invoice.getTotalServiceAmount());
        BigDecimal normalizedTestAmount = totalTestAmount != null ? totalTestAmount : BigDecimal.ZERO;
        res.setTotalTestAmount(normalizedTestAmount);
        res.setTotalExamServiceAmount(calculateTotalExamServiceAmount(invoice, normalizedTestAmount));
        res.setTotalMedicineAmount(invoice.getTotalMedicineAmount());
        res.setDiscountAmount(invoice.getDiscountAmount());
        res.setTotalAmount(invoice.getTotalAmount());
        res.setPaymentMethod(invoice.getPaymentMethod());
        res.setPaymentStatus(invoice.getPaymentStatus());
        res.setPaidAt(format(invoice.getPaidAt()));
        res.setNote(invoice.getNote());
        res.setCreatedAt(format(invoice.getCreatedAt()));
        res.setUpdatedAt(format(invoice.getUpdatedAt()));
        res.setActive(invoice.getActive());

        if (includePatient && invoice.getPatientId() != null) {
            res.setPatientId(invoice.getPatientId().getId());
            res.setPatientCode(invoice.getPatientId().getPatientCode());
            res.setPatientName(invoice.getPatientId().getFullName());
            res.setPatientPhone(invoice.getPatientId().getPhone());
        }

        if (includeMedicalRecord && invoice.getMedicalRecordId() != null) {
            res.setMedicalRecordId(invoice.getMedicalRecordId().getId());
            res.setMedicalRecordCode(invoice.getMedicalRecordId().getRecordCode());
        }
    }

    private static String format(java.util.Date date) {
        return date == null ? null : new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }

    private static BigDecimal calculateTotalExamServiceAmount(Invoice invoice, BigDecimal totalTestAmount) {
        BigDecimal totalServiceAmount = invoice.getTotalServiceAmount() != null
                ? invoice.getTotalServiceAmount()
                : BigDecimal.ZERO;
        BigDecimal normalizedTestAmount = totalTestAmount != null ? totalTestAmount : BigDecimal.ZERO;

        return totalServiceAmount.subtract(normalizedTestAmount).max(BigDecimal.ZERO);
    }

    private static AppointmentPatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        AppointmentPatientResponse res = new AppointmentPatientResponse();
        res.setId(patient.getId());
        res.setPatientCode(patient.getPatientCode());
        res.setFullName(patient.getFullName());
        res.setPhone(patient.getPhone());
        return res;
    }
}

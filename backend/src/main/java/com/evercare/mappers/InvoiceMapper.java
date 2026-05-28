package com.evercare.mappers;

import com.evercare.dtos.response.InvoiceDetailResponse;
import com.evercare.dtos.response.InvoiceResponse;
import com.evercare.dtos.response.PaymentResponse;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Payment;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

public final class InvoiceMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private InvoiceMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceResponse res = new InvoiceResponse();
        fillBase(invoice, res);
        return res;
    }

    public static InvoiceDetailResponse toDetailResponse(Invoice invoice, List<Payment> payments) {
        if (invoice == null) {
            return null;
        }

        InvoiceDetailResponse res = new InvoiceDetailResponse();
        fillBase(invoice, res);
        List<PaymentResponse> paymentResponses = payments == null
                ? Collections.emptyList()
                : payments.stream().map(PaymentMapper::toResponse).toList();
        res.setPayments(paymentResponses);
        return res;
    }

    private static void fillBase(Invoice invoice, InvoiceResponse res) {
        res.setId(invoice.getId());
        res.setInvoiceCode(invoice.getInvoiceCode());
        res.setTotalServiceAmount(invoice.getTotalServiceAmount());
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
        res.setMedicalRecordId(invoice.getMedicalRecordId() != null ? invoice.getMedicalRecordId().getId() : null);
        res.setPatientId(invoice.getPatientId() != null ? invoice.getPatientId().getId() : null);
    }

    private static String format(java.util.Date date) {
        return date == null ? null : new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }
}

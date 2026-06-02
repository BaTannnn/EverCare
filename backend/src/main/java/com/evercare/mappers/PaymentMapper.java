package com.evercare.mappers;

import com.evercare.dtos.response.PaymentResultResponse;
import com.evercare.dtos.response.PaymentResponse;
import com.evercare.dtos.response.InvoiceResponse;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Payment;
import java.text.SimpleDateFormat;

public final class PaymentMapper {
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private PaymentMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse res = new PaymentResponse();
        fillBase(payment, res);
        return res;
    }

    public static PaymentResultResponse toResultResponse(Payment payment, String paymentUrl) {
        PaymentResultResponse res = new PaymentResultResponse();
        fillBase(payment, res);
        res.setPaymentUrl(paymentUrl);
        if (payment != null && payment.getInvoiceId() != null) {
            res.setInvoice(InvoiceMapper.toResponse(payment.getInvoiceId()));
        }
        return res;
    }

    public static PaymentResultResponse toResultResponse(Payment payment, String paymentUrl, Invoice invoice) {
        PaymentResultResponse res = new PaymentResultResponse();
        fillBase(payment, res);
        res.setPaymentUrl(paymentUrl);
        res.setInvoice(invoice != null ? InvoiceMapper.toResponse(invoice) : null);
        return res;
    }

    private static void fillBase(Payment payment, PaymentResponse res) {
        res.setId(payment.getId());
        res.setInvoiceId(payment.getInvoiceId() != null ? payment.getInvoiceId().getId() : null);
        res.setInvoiceCode(payment.getInvoiceId() != null ? payment.getInvoiceId().getInvoiceCode() : null);
        res.setAmount(payment.getAmount());
        res.setPaymentMethod(payment.getPaymentMethod());
        res.setPaymentProvider(payment.getPaymentProvider());
        res.setTransactionCode(payment.getTransactionCode());
        res.setPaymentStatus(payment.getPaymentStatus());
        res.setPaidAt(format(payment.getPaidAt()));
        res.setCreatedAt(format(payment.getCreatedAt()));
        res.setUpdatedAt(format(payment.getUpdatedAt()));
        res.setActive(payment.getActive());
    }

    private static String format(java.util.Date date) {
        return date == null ? null : new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }
}

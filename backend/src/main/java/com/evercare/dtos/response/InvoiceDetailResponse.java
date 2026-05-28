package com.evercare.dtos.response;

import java.util.List;

public class InvoiceDetailResponse extends InvoiceResponse {
    private List<PaymentResponse> payments;

    public List<PaymentResponse> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentResponse> payments) {
        this.payments = payments;
    }
}

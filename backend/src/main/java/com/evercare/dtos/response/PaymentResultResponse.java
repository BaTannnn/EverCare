package com.evercare.dtos.response;

public class PaymentResultResponse extends PaymentResponse {
    private String paymentUrl;

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}

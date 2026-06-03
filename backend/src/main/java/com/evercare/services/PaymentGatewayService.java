package com.evercare.services;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.dtos.response.PaymentGatewayResultResponse;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Payment;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayService {
    boolean supports(String paymentMethod);
    String getProvider();
    String createPaymentUrl(Invoice invoice, Payment payment, PaymentRequest request);
    boolean verifyCallback(Map<String, String> params);
    PaymentGatewayResultResponse parseCallback(Map<String, String> params);
    PaymentGatewayResultResponse refund(Payment payment, BigDecimal amount, String reason);
}

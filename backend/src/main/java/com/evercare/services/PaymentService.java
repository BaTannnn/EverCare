package com.evercare.services;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.dtos.response.PaymentResultResponse;
import com.evercare.dtos.response.PaymentResponse;
import java.util.Map;

public interface PaymentService {
    PaymentResultResponse createPayment(Long invoiceId, PaymentRequest request);
    PaymentResultResponse createReceptionistPayment(Long invoiceId, PaymentRequest request);
    PaymentResponse handleGatewayCallback(String provider, Map<String, String> params);
    PaymentResponse handleGatewayResult(String provider, Map<String, String> params);
}

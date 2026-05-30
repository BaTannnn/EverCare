package com.evercare.services.impl;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Payment;
import com.evercare.dtos.response.PaymentGatewayResultResponse;
import com.evercare.services.PaymentGatewayService;
import com.evercare.utils.PaymentGatewaySupport;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class VnPayPaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final String METHOD = "VNPAY";

    @Autowired
    private Environment env;

    @Override
    public boolean supports(String paymentMethod) {
        return paymentMethod != null && METHOD.equalsIgnoreCase(paymentMethod.trim());
    }

    @Override
    public String getProvider() {
        return METHOD;
    }

    @Override
    public String createPaymentUrl(Invoice invoice, Payment payment, PaymentRequest request) {
        String payUrl = PaymentGatewaySupport.requireProperty(this.env, "payment.vnpay.payUrl");
        String tmnCode = PaymentGatewaySupport.requireProperty(this.env, "payment.vnpay.tmnCode");
        String hashSecret = PaymentGatewaySupport.requireProperty(this.env, "payment.vnpay.hashSecret");
        String returnUrl = buildBackendReturnUrl("payment.vnpay.returnUrl", request != null ? request.getReturnUrl() : null);
        String bankCode = resolveBankCode(request);
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String expireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis() + 15 * 60 * 1000L));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", toVnPayAmount(payment.getAmount()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getTransactionCode());
        params.put("vnp_OrderInfo", "Thanh toan hoa don " + invoice.getInvoiceCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);
        if (bankCode != null && !bankCode.isBlank()) {
            params.put("vnp_BankCode", bankCode.trim().toUpperCase());
        }

        String raw = PaymentGatewaySupport.buildQueryString(params);
        String signature = PaymentGatewaySupport.hmacSha512(hashSecret, raw);
        return payUrl + "?" + raw + "&vnp_SecureHash=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);
    }

    private String resolveBankCode(PaymentRequest request) {
        if (request == null) {
            return null;
        }

        if (request.getBankCode() != null && !request.getBankCode().isBlank()) {
            return request.getBankCode().trim();
        }

        if (request.getPaymentChannel() == null || request.getPaymentChannel().isBlank()) {
            return null;
        }

        String channel = request.getPaymentChannel().trim().toUpperCase();
        return switch (channel) {
            case "QR" -> "VNPAYQR";
            case "ATM", "BANK", "VNBANK" -> "VNBANK";
            case "INTCARD", "CARD", "CREDIT" -> "INTCARD";
            default -> null;
        };
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String hashSecret = PaymentGatewaySupport.optionalProperty(this.env, "payment.vnpay.hashSecret");
        if (hashSecret == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String secureHash = PaymentGatewaySupport.value(params, "vnp_SecureHash", "secureHash");
        if (secureHash == null) {
            return false;
        }

        Map<String, String> filtered = new HashMap<>(params);
        filtered.remove("vnp_SecureHash");
        filtered.remove("vnp_SecureHashType");
        filtered.remove("secureHash");
        filtered.remove("frontendReturnUrl");
        filtered.remove("paymentId");
        filtered.remove("invoiceId");
        filtered.remove("transactionCode");
        filtered.remove("paymentStatus");

        String raw = PaymentGatewaySupport.buildQueryString(filtered);
        String expected = PaymentGatewaySupport.hmacSha512(hashSecret, raw);
        return expected.equalsIgnoreCase(secureHash);
    }

    @Override
    public PaymentGatewayResultResponse parseCallback(Map<String, String> params) {
        PaymentGatewayResultResponse result = new PaymentGatewayResultResponse();
        result.setTransactionCode(PaymentGatewaySupport.value(params, "vnp_TxnRef", "txnRef"));
        result.setGatewayTransactionId(PaymentGatewaySupport.value(params, "vnp_TransactionNo", "transactionNo"));
        result.setAmount(PaymentGatewaySupport.amountFromScaledString(PaymentGatewaySupport.value(params, "vnp_Amount"), 100));
        String responseCode = PaymentGatewaySupport.value(params, "vnp_ResponseCode");
        String transactionStatus = PaymentGatewaySupport.value(params, "vnp_TransactionStatus");
        result.setSuccess("00".equals(responseCode) && "00".equals(transactionStatus));
        result.setMessage(PaymentGatewaySupport.value(params, "vnp_Message", "message"));
        return result;
    }

    private String buildBackendReturnUrl(String propertyKey, String frontendReturnUrl) {
        String backendUrl = PaymentGatewaySupport.requireProperty(this.env, propertyKey);
        if (frontendReturnUrl == null || frontendReturnUrl.isBlank()) {
            return backendUrl;
        }

        return PaymentGatewaySupport.appendQueryParam(backendUrl, "frontendReturnUrl", frontendReturnUrl.trim());
    }

    @Override
    public PaymentGatewayResultResponse refund(Payment payment, BigDecimal amount, String reason) {
        String endpoint = PaymentGatewaySupport.optionalProperty(this.env, "payment.vnpay.refundEndpoint");
        String tmnCode = PaymentGatewaySupport.optionalProperty(this.env, "payment.vnpay.tmnCode");
        String hashSecret = PaymentGatewaySupport.optionalProperty(this.env, "payment.vnpay.hashSecret");
        if (endpoint == null || tmnCode == null || hashSecret == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String requestId = "REFUND_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "_" + UUID.randomUUID().toString().substring(0, 8);
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "refund");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_TransactionType", "02");
        params.put("vnp_TxnRef", payment.getTransactionCode());
        params.put("vnp_Amount", toVnPayAmount(amount));
        params.put("vnp_OrderInfo", reason != null ? reason : "Hoan tien hoa don");
        params.put("vnp_TransactionNo", payment.getTransactionCode());
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_CreateBy", "system");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_RequestId", requestId);

        String raw = PaymentGatewaySupport.buildQueryString(params);
        String signature = PaymentGatewaySupport.hmacSha512(hashSecret, raw);
        String response = PaymentGatewaySupport.postForm(endpoint, raw + "&vnp_SecureHash=" + signature);
        JsonNode json = PaymentGatewaySupport.readJson(response);
        PaymentGatewayResultResponse result = new PaymentGatewayResultResponse();
        result.setTransactionCode(requestId);
        result.setAmount(amount);
        result.setGatewayTransactionId(payment.getTransactionCode());
        boolean success = json.hasNonNull("vnp_ResponseCode") && "00".equals(json.get("vnp_ResponseCode").asText());
        if (!success && json.hasNonNull("ResponseCode")) {
            success = "00".equals(json.get("ResponseCode").asText());
        }
        result.setSuccess(success);
        result.setMessage(json.hasNonNull("vnp_Message") ? json.get("vnp_Message").asText() : null);
        return result;
    }

    private String toVnPayAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }

        return amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString();
    }
}

package com.evercare.services.impl;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Payment;
import com.evercare.services.PaymentGatewayResult;
import com.evercare.services.PaymentGatewayService;
import com.evercare.utils.PaymentGatewaySupport;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class MomoPaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final String METHOD = "MOMO";
    private static final Logger logger = LoggerFactory.getLogger(MomoPaymentGatewayServiceImpl.class);

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
        String endpoint = PaymentGatewaySupport.requireProperty(this.env, "payment.momo.endpoint");
        String partnerCode = PaymentGatewaySupport.requireProperty(this.env, "payment.momo.partnerCode");
        String accessKey = PaymentGatewaySupport.requireProperty(this.env, "payment.momo.accessKey");
        String secretKey = PaymentGatewaySupport.requireProperty(this.env, "payment.momo.secretKey");
        String frontendReturnUrl = request != null ? request.getReturnUrl() : null;
        String returnUrl = buildBackendReturnUrl("payment.momo.returnUrl", frontendReturnUrl);
        String ipnUrl = PaymentGatewaySupport.requireProperty(this.env, "payment.momo.ipnUrl");
        String requestType = resolveRequestType(request);
        String requestId = payment.getTransactionCode();
        String orderId = payment.getTransactionCode();
        if (payment.getAmount() == null) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }

        if (payment.getAmount().stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải là số nguyên");
        }

        long amount = payment.getAmount().longValue();
        String amountValue = String.valueOf(amount);
        String orderInfo = "Thanh toan hoa don " + invoice.getInvoiceCode();
        String extraData = "";
        Map<String, String> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", amountValue);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", returnUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("lang", "vi");

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amountValue
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + returnUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        body.put("signature", PaymentGatewaySupport.hmacSha256(secretKey, rawSignature));

        String response = PaymentGatewaySupport.postJson(endpoint, PaymentGatewaySupport.toJson(body));
        JsonNode json = PaymentGatewaySupport.readJson(response);
        if (json.hasNonNull("payUrl")) {
            return json.get("payUrl").asText();
        }
        if (json.hasNonNull("deeplink")) {
            return json.get("deeplink").asText();
        }
        throw new IllegalStateException("Không thể tạo payment URL MoMo");
    }

    private String resolveRequestType(PaymentRequest request) {
        if (request == null || request.getPaymentChannel() == null || request.getPaymentChannel().isBlank()) {
            return "payWithMethod";
        }

        String channel = request.getPaymentChannel().trim().toUpperCase();
        return switch (channel) {
            case "WALLET" -> "captureWallet";
            case "ATM" -> "payWithATM";
            case "CC", "CARD", "CREDIT" -> "payWithCC";
            case "METHOD", "ALL" -> "payWithMethod";
            default -> "payWithMethod";
        };
    }

    private String buildBackendReturnUrl(String propertyKey, String frontendReturnUrl) {
        String backendUrl = PaymentGatewaySupport.requireProperty(this.env, propertyKey);
        if (frontendReturnUrl == null || frontendReturnUrl.isBlank()) {
            return backendUrl;
        }

        return PaymentGatewaySupport.appendQueryParam(backendUrl, "frontendReturnUrl", frontendReturnUrl.trim());
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String secretKey = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.secretKey");
        String accessKey = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.accessKey");
        if (secretKey == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }
        if (accessKey == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String signature = PaymentGatewaySupport.value(params, "signature", "sign");
        if (signature == null) {
            return false;
        }

        String raw = "accessKey=" + accessKey
                + "&amount=" + valueOrEmpty(params, "amount")
                + "&extraData=" + valueOrEmpty(params, "extraData")
                + "&message=" + valueOrEmpty(params, "message")
                + "&orderId=" + valueOrEmpty(params, "orderId")
                + "&orderInfo=" + valueOrEmpty(params, "orderInfo")
                + "&orderType=" + valueOrEmpty(params, "orderType")
                + "&partnerCode=" + valueOrEmpty(params, "partnerCode")
                + "&payType=" + valueOrEmpty(params, "payType")
                + "&requestId=" + valueOrEmpty(params, "requestId")
                + "&responseTime=" + valueOrEmpty(params, "responseTime")
                + "&resultCode=" + valueOrEmpty(params, "resultCode")
                + "&transId=" + valueOrEmpty(params, "transId");
        logger.debug("MoMo verify raw: {}", raw);
        String expected = PaymentGatewaySupport.hmacSha256(secretKey, raw);
        return expected.equalsIgnoreCase(signature);
    }

    @Override
    public PaymentGatewayResult parseCallback(Map<String, String> params) {
        PaymentGatewayResult result = new PaymentGatewayResult();
        result.setTransactionCode(PaymentGatewaySupport.value(params, "orderId", "requestId"));
        result.setGatewayTransactionId(PaymentGatewaySupport.value(params, "transId"));
        result.setAmount(PaymentGatewaySupport.amountFromString(PaymentGatewaySupport.value(params, "amount")));
        String resultCode = PaymentGatewaySupport.value(params, "resultCode");
        result.setSuccess("0".equals(resultCode));
        result.setMessage(PaymentGatewaySupport.value(params, "message"));
        return result;
    }

    @Override
    public PaymentGatewayResult refund(Payment payment, BigDecimal amount, String reason) {
        String endpoint = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.refundEndpoint");
        String partnerCode = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.partnerCode");
        String accessKey = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.accessKey");
        String secretKey = PaymentGatewaySupport.optionalProperty(this.env, "payment.momo.secretKey");
        if (endpoint == null || partnerCode == null || accessKey == null || secretKey == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String requestId = "REFUND_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, String> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("orderId", payment.getTransactionCode());
        body.put("transId", payment.getTransactionCode());
        body.put("amount", amount.toPlainString());
        body.put("lang", "vi");
        body.put("description", reason != null ? reason : "Hoan tien hoa don");

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount.toPlainString()
                + "&description=" + body.get("description")
                + "&orderId=" + payment.getTransactionCode()
                + "&partnerCode=" + partnerCode
                + "&requestId=" + requestId
                + "&transId=" + payment.getTransactionCode();
        body.put("signature", PaymentGatewaySupport.hmacSha256(secretKey, rawSignature));

        String response = PaymentGatewaySupport.postJson(endpoint, PaymentGatewaySupport.toJson(body));
        JsonNode json = PaymentGatewaySupport.readJson(response);
        PaymentGatewayResult result = new PaymentGatewayResult();
        result.setTransactionCode(requestId);
        result.setAmount(amount);
        result.setGatewayTransactionId(json.hasNonNull("transId") ? json.get("transId").asText() : null);
        boolean success = json.hasNonNull("resultCode") && "0".equals(json.get("resultCode").asText());
        result.setSuccess(success);
        result.setMessage(json.hasNonNull("message") ? json.get("message").asText() : null);
        return result;
    }

    private String valueOrEmpty(Map<String, String> params, String key) {
        String value = PaymentGatewaySupport.value(params, key);
        return value != null ? value : "";
    }
}

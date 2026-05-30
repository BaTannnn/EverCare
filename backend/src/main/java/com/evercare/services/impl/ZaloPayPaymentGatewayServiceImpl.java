package com.evercare.services.impl;

import com.evercare.dtos.request.PaymentRequest;
import com.evercare.pojo.Invoice;
import com.evercare.pojo.Payment;
import com.evercare.services.PaymentGatewayResult;
import com.evercare.services.PaymentGatewayService;
import com.evercare.utils.PaymentGatewaySupport;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
@PropertySource("classpath:payments.properties")
public class ZaloPayPaymentGatewayServiceImpl implements PaymentGatewayService {
    private static final String METHOD = "ZALOPAY";
    private static final Logger logger = LoggerFactory.getLogger(ZaloPayPaymentGatewayServiceImpl.class);

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
        String endpoint = PaymentGatewaySupport.requireProperty(this.env, "payment.zalopay.endpoint");
        String appId = PaymentGatewaySupport.requireProperty(this.env, "payment.zalopay.appId");
        String key1 = PaymentGatewaySupport.requireProperty(this.env, "payment.zalopay.key1");
        String callbackUrl = PaymentGatewaySupport.requireProperty(this.env, "payment.zalopay.callbackUrl");
        String appTransId = payment.getTransactionCode();
        String appTime = String.valueOf(System.currentTimeMillis());
        String amount = toZaloPayAmount(payment.getAmount());
        String description = "Thanh toan hoa don " + invoice.getInvoiceCode();
        String bankCode = resolveBankCode(request);
        String embedData = buildEmbedData(request, buildBackendReturnUrl("payment.zalopay.redirectUrl", request != null ? request.getReturnUrl() : null));

        Map<String, String> body = new HashMap<>();
        body.put("app_id", appId);
        body.put("app_trans_id", appTransId);
        body.put("app_user", invoice.getPatientId() != null && invoice.getPatientId().getUserId() != null
                ? invoice.getPatientId().getUserId().getUsername()
                : "patient");
        body.put("app_time", appTime);
        body.put("amount", amount);
        body.put("bank_code", bankCode);
        body.put("callback_url", callbackUrl);
        body.put("description", description);
        body.put("embed_data", embedData);
        body.put("item", "[]");

        String rawSignature = appId + "|" + appTransId + "|" + body.get("app_user") + "|" + amount + "|" + appTime + "|" + body.get("embed_data") + "|" + body.get("item");
        body.put("mac", PaymentGatewaySupport.hmacSha256(key1, rawSignature));

        logger.debug("ZaloPay create order payload: {}", PaymentGatewaySupport.toJson(body));
        String response = PaymentGatewaySupport.postWithQueryParams(endpoint, body);
        logger.debug("ZaloPay create order response: {}", response);
        JsonNode json = PaymentGatewaySupport.readJson(response);
        if (isSuccess(json) && json.hasNonNull("order_url")) {
            return json.get("order_url").asText();
        }
        if (isSuccess(json) && json.hasNonNull("orderUrl")) {
            return json.get("orderUrl").asText();
        }
        if (isSuccess(json) && json.hasNonNull("zp_trans_token")) {
            return json.get("zp_trans_token").asText();
        }
        throw new IllegalStateException(buildCreateOrderError(json, response));
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String key2 = PaymentGatewaySupport.optionalProperty(this.env, "payment.zalopay.key2");
        if (key2 == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String mac = PaymentGatewaySupport.value(params, "mac", "signature");
        String data = PaymentGatewaySupport.value(params, "data");
        if (mac == null || data == null) {
            return false;
        }

        String expected = PaymentGatewaySupport.hmacSha256(key2, data);
        return expected.equalsIgnoreCase(mac);
    }

    @Override
    public PaymentGatewayResult parseCallback(Map<String, String> params) {
        PaymentGatewayResult result = new PaymentGatewayResult();
        String data = PaymentGatewaySupport.value(params, "data");
        if (data != null && data.startsWith("{")) {
            JsonNode json = PaymentGatewaySupport.readJson(data);
            result.setTransactionCode(json.hasNonNull("app_trans_id") ? json.get("app_trans_id").asText() : PaymentGatewaySupport.value(params, "app_trans_id"));
            result.setGatewayTransactionId(json.hasNonNull("zp_trans_token") ? json.get("zp_trans_token").asText() : null);
            result.setAmount(json.hasNonNull("amount") ? json.get("amount").decimalValue() : PaymentGatewaySupport.amountFromString(PaymentGatewaySupport.value(params, "amount")));
            result.setSuccess(json.hasNonNull("status") && ("1".equals(json.get("status").asText()) || "2".equals(json.get("status").asText())));
            result.setMessage(json.hasNonNull("message") ? json.get("message").asText() : null);
            return result;
        }

        result.setTransactionCode(PaymentGatewaySupport.value(params, "app_trans_id", "transaction_code"));
        result.setGatewayTransactionId(PaymentGatewaySupport.value(params, "zp_trans_token"));
        result.setAmount(PaymentGatewaySupport.amountFromString(PaymentGatewaySupport.value(params, "amount")));
        String status = PaymentGatewaySupport.value(params, "status", "return_code");
        result.setSuccess("1".equals(status) || "2".equals(status));
        result.setMessage(PaymentGatewaySupport.value(params, "message", "desc"));
        return result;
    }

    @Override
    public PaymentGatewayResult refund(Payment payment, BigDecimal amount, String reason) {
        String endpoint = PaymentGatewaySupport.optionalProperty(this.env, "payment.zalopay.refundEndpoint");
        String appId = PaymentGatewaySupport.optionalProperty(this.env, "payment.zalopay.appId");
        String key1 = PaymentGatewaySupport.optionalProperty(this.env, "payment.zalopay.key1");
        if (endpoint == null || appId == null || key1 == null) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }

        String refundId = "REFUND_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "_" + UUID.randomUUID().toString().substring(0, 8);
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, String> body = new HashMap<>();
        body.put("appid", appId);
        body.put("m_refund_id", refundId);
        body.put("timestamp", timestamp);
        body.put("zp_trans_id", payment.getTransactionCode());
        body.put("amount", amount.toPlainString());
        body.put("description", reason != null ? reason : "Hoan tien hoa don");

        String rawSignature = appId + "|" + payment.getTransactionCode() + "|" + amount.toPlainString() + "|" + timestamp + "|" + refundId;
        body.put("mac", PaymentGatewaySupport.hmacSha256(key1, rawSignature));

        String response = PaymentGatewaySupport.postJson(endpoint, PaymentGatewaySupport.toJson(body));
        JsonNode json = PaymentGatewaySupport.readJson(response);
        PaymentGatewayResult result = new PaymentGatewayResult();
        result.setTransactionCode(refundId);
        result.setAmount(amount);
        result.setGatewayTransactionId(payment.getTransactionCode());
        boolean success = json.hasNonNull("return_code") && "1".equals(json.get("return_code").asText());
        if (!success && json.hasNonNull("status")) {
            success = "1".equals(json.get("status").asText());
        }
        result.setSuccess(success);
        result.setMessage(json.hasNonNull("return_message") ? json.get("return_message").asText() : null);
        return result;
    }

    private String buildBackendReturnUrl(String propertyKey, String frontendReturnUrl) {
        String backendUrl = PaymentGatewaySupport.requireProperty(this.env, propertyKey);
        if (frontendReturnUrl == null || frontendReturnUrl.isBlank()) {
            return backendUrl;
        }

        return PaymentGatewaySupport.appendQueryParam(backendUrl, "frontendReturnUrl", frontendReturnUrl.trim());
    }

    private String toZaloPayAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }

        return amount.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
    }

    private String buildEmbedData(PaymentRequest request, String redirectUrl) {
        String preferredMethod = resolvePreferredPaymentMethod(request);
        StringBuilder builder = new StringBuilder();
        builder.append("{\"redirecturl\":\"").append(escapeJson(redirectUrl)).append("\",");
        builder.append("\"preferred_payment_method\":");
        if (preferredMethod == null) {
            builder.append("[]");
        } else {
            builder.append("[");
            String[] methods = preferredMethod.split(",");
            for (int i = 0; i < methods.length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append("\"").append(methods[i]).append("\"");
            }
            builder.append("]");
        }
        builder.append("}");
        return builder.toString();
    }

    private String resolvePreferredPaymentMethod(PaymentRequest request) {
        if (request == null || request.getPaymentChannel() == null || request.getPaymentChannel().isBlank()) {
            return null;
        }

        String channel = request.getPaymentChannel().trim().toUpperCase();
        return switch (channel) {
            case "QR" -> "vietqr";
            case "WALLET" -> "zalopay_wallet";
            case "ATM", "BANK" -> "domestic_card,account";
            case "CARD", "CREDIT", "INTCARD" -> "international_card";
            default -> null;
        };
    }

    private String resolveBankCode(PaymentRequest request) {
        if (request != null && request.getBankCode() != null && !request.getBankCode().isBlank()) {
            return request.getBankCode().trim();
        }
        return "";
    }

    private boolean isSuccess(JsonNode json) {
        if (json == null || !json.hasNonNull("return_code")) {
            return false;
        }

        String value = json.get("return_code").asText();
        return "1".equals(value) || "01".equals(value);
    }

    private String buildCreateOrderError(JsonNode json, String rawResponse) {
        StringBuilder builder = new StringBuilder("Không thể tạo payment URL ZaloPay");
        if (json != null) {
            if (json.hasNonNull("return_code")) {
                builder.append("; return_code=").append(json.get("return_code").asText());
            }
            if (json.hasNonNull("sub_return_code")) {
                builder.append("; sub_return_code=").append(json.get("sub_return_code").asText());
            }
            if (json.hasNonNull("return_message")) {
                builder.append("; return_message=").append(json.get("return_message").asText());
            }
            if (json.hasNonNull("sub_return_message")) {
                builder.append("; sub_return_message=").append(json.get("sub_return_message").asText());
            }
        } else if (rawResponse != null && !rawResponse.isBlank()) {
            builder.append("; response=").append(rawResponse);
        }
        return builder.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

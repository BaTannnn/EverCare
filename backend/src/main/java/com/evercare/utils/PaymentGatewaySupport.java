package com.evercare.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.core.env.Environment;

public final class PaymentGatewaySupport {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PaymentGatewaySupport() {
    }

    public static String requireProperty(Environment env, String key) {
        String value = env.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Payment gateway is not configured.");
        }
        return value.trim();
    }

    public static String optionalProperty(Environment env, String key) {
        String value = env.getProperty(key);
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public static String hmacSha256(String secret, String data) {
        return hmac("HmacSHA256", secret, data);
    }

    public static String hmacSha512(String secret, String data) {
        return hmac("HmacSHA512", secret, data);
    }

    public static String sha256Hex(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo hash", ex);
        }
    }

    public static String buildQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(params).entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return builder.toString();
    }

    public static String buildSignedQueryString(Map<String, String> params, String signatureKey, String signatureValue) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        if (builder.length() > 0) {
            builder.append('&');
        }
        builder.append(signatureKey).append('=').append(encode(signatureValue));
        return builder.toString();
    }

    public static String appendQueryParam(String url, String key, String value) {
        if (url == null || url.isBlank() || key == null || key.isBlank() || value == null) {
            return url;
        }

        String separator = url.contains("?") ? "&" : "?";
        return url + separator + encode(key) + "=" + encode(value);
    }

    public static String appendQueryParam(String url, String key, String value, boolean keepBlank) {
        if (!keepBlank && (value == null || value.isBlank())) {
            return url;
        }
        return appendQueryParam(url, key, value == null ? "" : value);
    }

    public static String postJson(String endpoint, String jsonBody) {
        return send(endpoint, "POST", "application/json", jsonBody);
    }

    public static String postForm(String endpoint, String formBody) {
        return send(endpoint, "POST", "application/x-www-form-urlencoded", formBody);
    }

    public static String postWithQueryParams(String endpoint, Map<String, String> params) {
        return send(endpoint + "?" + buildQueryString(params), "POST", "application/x-www-form-urlencoded", "");
    }

    public static JsonNode readJson(String content) {
        try {
            return OBJECT_MAPPER.readTree(content);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể đọc phản hồi payment gateway", ex);
        }
    }

    public static String toJson(Map<String, ?> body) {
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể tạo request payment gateway", ex);
        }
    }

    public static String value(Map<String, String> params, String... keys) {
        for (String key : keys) {
            if (params.containsKey(key) && params.get(key) != null && !params.get(key).isBlank()) {
                return params.get(key);
            }
        }
        return null;
    }

    public static BigDecimal amountFromString(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    public static BigDecimal amountFromScaledString(String value, int scaleFactor) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim()).divide(BigDecimal.valueOf(scaleFactor));
    }

    private static String send(String endpoint, String method, String contentType, String body) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", contentType)
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Payment gateway trả về lỗi HTTP " + response.statusCode() + ": " + response.body()
                );
            }
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Không thể gọi payment gateway: request bị gián đoạn", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể gọi payment gateway: " + ex.getMessage(), ex);
        }
    }

    private static String hmac(String algorithm, String secret, String data) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo chữ ký payment gateway", ex);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

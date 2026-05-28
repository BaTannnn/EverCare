package com.evercare.controllers.api;

import com.evercare.dtos.response.PaymentResponse;
import com.evercare.services.PaymentService;
import com.evercare.utils.PaymentGatewaySupport;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class ApiPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(ApiPaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/momo/ipn")
    public ResponseEntity<PaymentResponse> momo(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam Map<String, String> params
    ) {
        return ResponseEntity.ok(this.paymentService.handleGatewayCallback("MOMO", mergeParams(body, params)));
    }

    @PostMapping("/zalopay/callback")
    public ResponseEntity<PaymentResponse> zaloPay(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam Map<String, String> params
    ) {
        Map<String, String> callbackParams = mergeParams(body, params);
        logger.debug("ZaloPay callback endpoint params: {}", callbackParams);
        return ResponseEntity.ok(this.paymentService.handleGatewayCallback("ZALOPAY", callbackParams));
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnPay(@RequestParam Map<String, String> params) {
        try {
            this.paymentService.handleGatewayCallback("VNPAY", params);
            return ResponseEntity.ok(vnpayResponse("00", "Confirm Success"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(vnpayResponse(resolveVnPayCode(ex), ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.ok(vnpayResponse("01", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.ok(vnpayResponse("99", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.ok(vnpayResponse("99", "Unknown error"));
        }
    }

    @GetMapping("/momo/result")
    public ResponseEntity<?> momoResult(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        return handleResult("MOMO", params, request);
    }

    @GetMapping("/zalopay/result")
    public ResponseEntity<?> zaloPayResult(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        return handleResult("ZALOPAY", params, request);
    }

    @GetMapping("/vnpay/result")
    public ResponseEntity<?> vnPayResult(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        return handleResult("VNPAY", params, request);
    }

    private Map<String, String> mergeParams(Map<String, Object> body, Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        if (params != null) {
            result.putAll(params);
        }

        if (body != null) {
            for (Map.Entry<String, Object> entry : body.entrySet()) {
                result.put(entry.getKey(), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    private ResponseEntity<?> handleResult(String provider, Map<String, String> params, HttpServletRequest request) {
        logger.debug("{} result endpoint params: {}", provider, params);
        Map<String, String> callbackParams = new LinkedHashMap<>(params);
        callbackParams.remove("frontendReturnUrl");
        PaymentResponse response = this.paymentService.handleGatewayResult(provider, callbackParams);
        String frontendReturnUrl = params.get("frontendReturnUrl");
        if (frontendReturnUrl != null && !frontendReturnUrl.isBlank()) {
            String redirectUrl = PaymentGatewaySupport.appendQueryParam(frontendReturnUrl.trim(), "paymentId", response.getId() != null ? String.valueOf(response.getId()) : null);
            redirectUrl = PaymentGatewaySupport.appendQueryParam(redirectUrl, "invoiceId", response.getInvoiceId() != null ? String.valueOf(response.getInvoiceId()) : null);
            redirectUrl = PaymentGatewaySupport.appendQueryParam(redirectUrl, "transactionCode", response.getTransactionCode());
            redirectUrl = PaymentGatewaySupport.appendQueryParam(redirectUrl, "paymentStatus", response.getPaymentStatus());
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, String> vnpayResponse(String rspCode, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", rspCode);
        response.put("Message", message);
        return response;
    }

    private String resolveVnPayCode(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (message.contains("không khớp") || message.contains("khong khop") || message.contains("amount")) {
            return "04";
        }
        if (message.contains("chữ ký") || message.contains("chu ky") || message.contains("signature")) {
            return "97";
        }
        if (message.contains("định dạng") || message.contains("dinh dang")) {
            return "03";
        }
        return "99";
    }

}

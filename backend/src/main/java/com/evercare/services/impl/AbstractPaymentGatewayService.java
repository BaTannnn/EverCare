package com.evercare.services.impl;

import com.evercare.services.PaymentGatewayService;
import com.evercare.utils.PaymentGatewaySupport;
import org.springframework.core.env.Environment;

public abstract class AbstractPaymentGatewayService implements PaymentGatewayService {
    protected abstract String getMethod();

    @Override
    public boolean supports(String paymentMethod) {
        return paymentMethod != null && getMethod().equalsIgnoreCase(paymentMethod.trim());
    }

    @Override
    public String getProvider() {
        return getMethod();
    }

    protected String buildBackendReturnUrl(Environment env, String propertyKey, String frontendReturnUrl) {
        String backendUrl = PaymentGatewaySupport.requireProperty(env, propertyKey);
        if (frontendReturnUrl == null || frontendReturnUrl.isBlank()) {
            return backendUrl;
        }

        return PaymentGatewaySupport.appendQueryParam(backendUrl, "frontendReturnUrl", frontendReturnUrl.trim());
    }
}

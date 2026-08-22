package com.evercare.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class PaymentGatewaySupportTest {
    @Test
    void buildsSortedEncodedQueryString() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("z", "hello world");
        params.put("a", "1+2");

        assertEquals("a=1%2B2&z=hello+world", PaymentGatewaySupport.buildQueryString(params));
    }

    @Test
    void appendsQueryParametersSafely() {
        assertEquals(
                "https://example.test/result?invoiceId=10&status=success",
                PaymentGatewaySupport.appendQueryParam(
                        PaymentGatewaySupport.appendQueryParam("https://example.test/result", "invoiceId", "10"),
                        "status",
                        "success"
                )
        );
    }

    @Test
    void convertsScaledGatewayAmount() {
        assertEquals(new BigDecimal("125000"), PaymentGatewaySupport.amountFromScaledString("12500000", 100));
    }

    @Test
    void requiresConfiguredGatewayProperty() {
        MockEnvironment environment = new MockEnvironment();
        assertThrows(
                IllegalStateException.class,
                () -> PaymentGatewaySupport.requireProperty(environment, "payment.secret")
        );
    }
}

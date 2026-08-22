package com.evercare.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SensitiveDataSanitizerTest {
    private final SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();

    @Test
    void masksEmailPhoneAndLongIdentifiers() {
        String result = sanitizer.sanitize(
                "Liên hệ tan@example.com, 0912345678, mã định danh 123456789012"
        );

        assertTrue(result.contains("[EMAIL]"));
        assertTrue(result.contains("[PHONE]") || result.contains("[IDENTIFIER]"));
        assertTrue(result.contains("[IDENTIFIER]"));
        assertFalse(result.contains("tan@example.com"));
        assertFalse(result.contains("123456789012"));
    }

    @Test
    void keepsNormalMedicalConversationText() {
        String text = "Tôi muốn đặt lịch khám nội tổng quát vào buổi sáng.";
        assertTrue(sanitizer.sanitize(text).contains("đặt lịch khám"));
    }
}

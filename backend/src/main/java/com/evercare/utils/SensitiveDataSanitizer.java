package com.evercare.utils;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SensitiveDataSanitizer {
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern VIETNAM_PHONE = Pattern.compile("(?<!\\d)(?:\\+?84|0)(?:[ .-]?\\d){9,10}(?!\\d)");
    private static final Pattern LONG_IDENTIFIER = Pattern.compile("(?<!\\d)\\d{9,12}(?!\\d)");

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input == null ? "" : input;
        }

        String sanitized = EMAIL.matcher(input).replaceAll("[EMAIL]");
        sanitized = VIETNAM_PHONE.matcher(sanitized).replaceAll("[PHONE]");
        return LONG_IDENTIFIER.matcher(sanitized).replaceAll("[IDENTIFIER]");
    }
}

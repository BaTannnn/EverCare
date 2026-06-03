package com.evercare.enums;

public enum PaymentStatus {
    PENDING("Cho thanh toan"),
    SUCCESS("Thanh cong"),
    FAILED("That bai"),
    REFUNDED("Da hoan tien");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return PENDING.name();
        }

        for (PaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trang thai thanh toan khong hop le");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (PaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }
}

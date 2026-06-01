package com.evercare.enums;

public enum InvoiceStatus {
    UNPAID("Chua thanh toan"),
    PARTIALLY_PAID("Thanh toan mot phan"),
    PAID("Da thanh toan"),
    REFUNDED("Da hoan tien");

    private final String label;

    InvoiceStatus(String label) {
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
            return UNPAID.name();
        }

        for (InvoiceStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trang thai hoa don khong hop le");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (InvoiceStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }
}

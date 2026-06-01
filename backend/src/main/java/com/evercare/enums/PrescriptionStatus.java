package com.evercare.enums;

public enum PrescriptionStatus {
    PRESCRIBED("Da ke don"),
    DISPENSED("Da cap phat"),
    CANCELLED("Da huy");

    private final String label;

    PrescriptionStatus(String label) {
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
            return PRESCRIBED.name();
        }

        for (PrescriptionStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trang thai don thuoc khong hop le");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (PrescriptionStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }
}

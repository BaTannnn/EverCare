package com.evercare.enums;

public enum MedicalRecordServiceStatus {
    ORDERED("Da chi dinh"),
    COMPLETED("Da co ket qua");

    private final String label;

    MedicalRecordServiceStatus(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public static MedicalRecordServiceStatus fromHasActiveResult(boolean hasActiveResult) {
        return hasActiveResult ? COMPLETED : ORDERED;
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return ORDERED.name();
        }

        for (MedicalRecordServiceStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trang thai chi dinh dich vu khong hop le");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (MedicalRecordServiceStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }
}

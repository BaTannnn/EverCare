package com.evercare.enums;

public enum MedicalServiceType {

    EXAMINATION("Khám bệnh"),
    CONSULTATION("Tư vấn"),
    TEST("Xét nghiệm"),
    IMAGING("Chẩn đoán hình ảnh"),
    PROCEDURE("Thủ thuật"),
    OTHER("Khác");

    private final String label;

    MedicalServiceType(String label) {
        this.label = label;
    }

    public String getCode() {
        return this.name();
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        for (MedicalServiceType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return true;
            }
        }

        return false;
    }

    public static String normalize(String code) {
        if (!isValid(code)) {
            throw new IllegalArgumentException("Loại dịch vụ không hợp lệ");
        }

        return code.trim().toUpperCase();
    }
}
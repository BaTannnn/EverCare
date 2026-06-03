package com.evercare.enums;

public enum DoctorType {

    FULL_TIME("Toàn thời gian"),
    PART_TIME("Bán thời gian"),
    CONSULTANT("Cộng tác viên");

    private final String label;

    DoctorType(String label) {
        this.label = label;
    }

    public String getCode() {
        return this.name();
    }

    public String getLabel() {
        return label;
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return FULL_TIME.name();
        }

        for (DoctorType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type.name();
            }
        }

        throw new IllegalArgumentException("Loại bác sĩ không hợp lệ");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (DoctorType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type.getLabel();
            }
        }

        return code;
    }
}
package com.evercare.enums;

public enum DoctorWorkStatus {

    AVAILABLE("Đang làm việc"),
    BUSY("Đang bận"),
    OFF_DUTY("Nghỉ ca"),
    INACTIVE("Ngưng hoạt động");

    private final String label;

    DoctorWorkStatus(String label) {
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
            return AVAILABLE.name();
        }

        for (DoctorWorkStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trạng thái làm việc không hợp lệ");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (DoctorWorkStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }
}
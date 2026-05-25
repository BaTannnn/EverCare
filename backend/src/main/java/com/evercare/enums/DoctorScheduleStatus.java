package com.evercare.enums;

public enum DoctorScheduleStatus {
    AVAILABLE("Đang mở"),
    FULL("Đã đầy"),
    CLOSED("Đã đóng"),
    CANCELLED("Đã hủy");

    private final String label;

    DoctorScheduleStatus(String label) {
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

        for (DoctorScheduleStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trạng thái lịch không hợp lệ");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (DoctorScheduleStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }
}

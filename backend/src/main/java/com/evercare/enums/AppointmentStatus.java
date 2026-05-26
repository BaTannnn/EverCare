package com.evercare.enums;

public enum AppointmentStatus {
    BOOKED("Đã đặt lịch"),
    WAITING("Bệnh nhân đã đến / đang chờ"),
    IN_PROGRESS("Đang khám"),
    COMPLETED("Đã khám xong"),
    CANCELLED("Đã hủy"),
    NO_SHOW("Bệnh nhân không đến");

    private final String label;

    AppointmentStatus(String label) {
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
            return BOOKED.name();
        }

        for (AppointmentStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.name();
            }
        }

        throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ");
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        for (AppointmentStatus status : values()) {
            if (status.name().equalsIgnoreCase(code.trim())) {
                return status.getLabel();
            }
        }

        return code;
    }

    public static boolean canStartExamination(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String normalizedCode = code.trim();
        return BOOKED.name().equalsIgnoreCase(normalizedCode)
                || WAITING.name().equalsIgnoreCase(normalizedCode);
    }
}

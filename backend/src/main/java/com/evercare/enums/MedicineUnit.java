package com.evercare.enums;

public enum MedicineUnit {
    VIEN,
    VI,
    ONG,
    CHAI,
    GOI,
    HOP,
    TUYP,
    LO;

    public String getCode() {
        return this.name();
    }

    public static String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Đơn vị thuốc không được để trống");
        }

        for (MedicineUnit unit : values()) {
            if (unit.name().equalsIgnoreCase(code.trim())) {
                return unit.name();
            }
        }

        throw new IllegalArgumentException("Đơn vị thuốc không hợp lệ");
    }
}

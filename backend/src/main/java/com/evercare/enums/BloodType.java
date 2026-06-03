package com.evercare.enums;

public enum BloodType {
    A("A"),
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B("B"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB("AB"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O("O"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String label;

    BloodType(String label) {
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
            throw new IllegalArgumentException("Blood type không hợp lệ");
        }

        String trimmed = code.trim().toUpperCase();
        for (BloodType bloodType : values()) {
            if (bloodType.name().equals(trimmed) || bloodType.label.equalsIgnoreCase(trimmed)) {
                return bloodType.label;
            }
        }

        throw new IllegalArgumentException("Blood type không hợp lệ");
    }
}

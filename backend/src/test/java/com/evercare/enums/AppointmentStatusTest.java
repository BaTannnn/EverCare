package com.evercare.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppointmentStatusTest {
    @Test
    void doctorCanStartOnlyFromWaiting() {
        assertTrue(AppointmentStatus.canStartExamination("WAITING"));
        assertFalse(AppointmentStatus.canStartExamination("BOOKED"));
        assertFalse(AppointmentStatus.canStartExamination("IN_PROGRESS"));
    }

    @Test
    void normalizesKnownStatusCaseInsensitively() {
        assertTrue("COMPLETED".equals(AppointmentStatus.normalize("completed")));
    }

    @Test
    void rejectsUnknownStatus() {
        assertThrows(IllegalArgumentException.class, () -> AppointmentStatus.normalize("UNKNOWN"));
    }
}

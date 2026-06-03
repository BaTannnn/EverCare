package com.evercare.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public final class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalTime toLocalTime(Date value) {
        if (value == null) {
            return null;
        }

        if (value instanceof java.sql.Time sqlTime) {
            return sqlTime.toLocalTime();
        }

        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public static LocalTime toNormalizedLocalTime(Date value) {
        LocalTime localTime = toLocalTime(value);
        if (localTime == null) {
            return null;
        }

        return localTime.withSecond(0).withNano(0);
    }
}

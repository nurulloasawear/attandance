package com.attendance.commonlib.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final String ISO_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ISO_PATTERN);

    private DateUtils() {
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(formatter);
    }

    public static LocalDateTime parse(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        return LocalDateTime.parse(dateStr, formatter);
    }
}

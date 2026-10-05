package com.localsearch.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility for friendly date/time formatting.
 */
public final class DateUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter FULL_FORMATTER = DateTimeFormatter.ofPattern("d MMM uuuu, h:mm a", Locale.ENGLISH);

    private DateUtils() {}

    /**
     * Formats a millisecond timestamp to a friendly relative/absolute label.
     * Examples: "Today, 10:42 PM", "Yesterday, 3:15 PM", "24 Sep 2026"
     */
    public static String formatFriendly(long timestamp) {
        if (timestamp <= 0) return "Never";

        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate itemDate = instant.atZone(zoneId).toLocalDate();
        LocalDate today = LocalDate.now(zoneId);

        if (itemDate.equals(today)) {
            return "Today, " + instant.atZone(zoneId).format(TIME_FORMATTER);
        } else if (itemDate.equals(today.minusDays(1))) {
            return "Yesterday, " + instant.atZone(zoneId).format(TIME_FORMATTER);
        } else if (itemDate.getYear() == today.getYear()) {
            return instant.atZone(zoneId).format(DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.ENGLISH));
        } else {
            return instant.atZone(zoneId).format(DATE_FORMATTER);
        }
    }

    /**
     * Formats to "24 Sep 2026".
     */
    public static String formatDate(long timestamp) {
        if (timestamp <= 0) return "Unknown";
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(DATE_FORMATTER);
    }

    /**
     * Formats to "24 Sep 2026, 10:42 PM".
     */
    public static String formatFull(long timestamp) {
        if (timestamp <= 0) return "Unknown";
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(FULL_FORMATTER);
    }
}

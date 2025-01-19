package com.laundry.util;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Format {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of("UTC"));

    /**
     * Formats the given {@link Instant} into a UTC date-time string.
     * If the provided Instant is null, returns null.
     *
     * @param instant the {@link Instant} to format
     * @return a string formatted as "yyyy-MM-dd HH:mm:ss" in UTC, or null if input is null
     */
    public static String format(Instant instant) {
        if (instant == null) {
            return null;
        }
        return FORMATTER.format(instant);
    }

    /**
     * Returns a string where the first character is uppercase and the remaining characters
     * are lowercase. If the input string is null or blank, the original value is returned.
     * <p>
     * Example conversions:
     * <ul>
     *   <li>null -> null</li>
     *   <li>"" -> ""</li>
     *   <li>"name" -> "Name"</li>
     *   <li>"nAMe" -> "Name"</li>
     *   <li>"NAME" -> "Name"</li>
     * </ul>
     *
     * @param input the original string to be capitalized
     * @return the capitalized string, or the original if null/blank
     */
    public static String capitalizeString(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}

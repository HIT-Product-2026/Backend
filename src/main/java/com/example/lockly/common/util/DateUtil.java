package com.example.lockly.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;


public class DateUtil {

    private static final DateTimeFormatter DD_MM_YYYY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Chuyển chuỗi định dạng dd/MM/yyyy thành LocalDate.
     *
     * @param date Chuỗi ngày tháng, ví dụ: 31/12/2000
     * @return LocalDate tương ứng
     * @throws IllegalArgumentException nếu định dạng hoặc ngày không hợp lệ
     */
    public static LocalDate parseDdMmYyyy(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(date, DD_MM_YYYY_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date. Expected format: dd/MM/yyyy", e);
        }
    }
}

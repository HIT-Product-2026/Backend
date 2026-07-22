package com.example.lockly.common.response;

import com.example.lockly.constant.ApiCode;

import java.time.LocalDateTime;
import java.util.List;

public record ListResponse<T>(
        int total,
        List<T> items,
        String cursor
) {

    public static <T> ListResponse<T> of(List<T> items) {
        return new ListResponse<>(items.size(), items, null);
    }

    public static <T> ListResponse<T> of(List<T> items, String cursor) {
        return new ListResponse<>(items.size(), items, cursor);
    }
}

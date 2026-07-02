package com.example.lockly.common.response;

import com.example.lockly.constant.ApiCode;

import java.time.LocalDateTime;
import java.util.List;

public record ListResponse<T>(
        int total,
        List<T> items
) {

    public static <T> ListResponse<T> of(List<T> items) {
        return new ListResponse<>(items.size(), items);
    }
}

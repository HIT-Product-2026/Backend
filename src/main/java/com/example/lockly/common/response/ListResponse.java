package com.example.lockly.common.response;

import com.example.lockly.constant.ApiCode;

import java.time.LocalDateTime;
import java.util.List;

public record ListResponse<T>(
        int code,
        String message,
        int total,
        List<T> items,
        LocalDateTime timestamp
) {

    public static <T> ListResponse<T> success(String message ,List<T> items) {
        return new ListResponse<>(ApiCode.SUCCESS, message, items.size(), items, LocalDateTime.now());
    }

    public static <T> ListResponse success(String message){
        return new ListResponse<>(ApiCode.SUCCESS, message, 0,null, LocalDateTime.now());
    }

    public static <T> ListResponse<T> error(int code, String message){
        return new ListResponse<>(ApiCode.BAD_REQUEST, message, 0, null, LocalDateTime.now());
    }

    public static <T> ListResponse<T> error(String message, List<T> items){
        return new ListResponse<>(ApiCode.BAD_REQUEST, message, items.size(), items, LocalDateTime.now());
    }

}

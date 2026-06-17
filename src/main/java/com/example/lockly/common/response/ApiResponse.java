package com.example.lockly.common.response;

import com.example.lockly.constant.ApiCode;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ApiCode.SUCCESS, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message){
        return new ApiResponse<>(ApiCode.SUCCESS, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(String message, T data){
        return new ApiResponse<>(ApiCode.CREATED, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message){
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message, T data){
        return new ApiResponse<>(code, message, data, LocalDateTime.now());
    }
}

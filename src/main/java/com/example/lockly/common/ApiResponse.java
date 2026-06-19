package com.example.lockly.common;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        int code,
        String message,
        boolean success,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(int code, String message, T data) {
        return new ApiResponse<>(code, message,true, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(int code, String message){
        return new ApiResponse<>(code, message, true,null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message){
        return new ApiResponse<>(code, message,false, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message, T data){
        return new ApiResponse<>(code, message,false, data, LocalDateTime.now());
    }
}

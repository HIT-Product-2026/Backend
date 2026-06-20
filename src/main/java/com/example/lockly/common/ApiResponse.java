package com.example.lockly.common;

import java.time.LocalDateTime;

public record ApiResponse<T>(
<<<<<<< HEAD
=======
        boolean success,
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        int code,
        String message,
        T data,
        LocalDateTime timestamp
) {
<<<<<<< HEAD
    public static <T> ApiResponse<T> ok(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(int code, String message){
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message){
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message, T data){
        return new ApiResponse<>(code, message, data, LocalDateTime.now());
    }
}
=======
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, 200, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, 200, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, 201, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(false, code, message, data, LocalDateTime.now());
    }
}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

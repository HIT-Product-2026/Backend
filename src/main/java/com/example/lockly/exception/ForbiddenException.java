package com.example.lockly.exception;

public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(403, message);
    }

    public ForbiddenException(String fieldValue, Object findValue) {
        super(403, String.format("Không có quyền truy cập với %s: %s", fieldValue, findValue));
    }
}
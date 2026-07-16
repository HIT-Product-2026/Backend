package com.example.lockly.exception.nonRetryException;

public class ForbiddenException extends NonRetryableAppException {

    public ForbiddenException(String message) {
        super(403, message);
    }

    public ForbiddenException(String fieldValue, Object findValue) {
        super(403, String.format("Không có quyền truy cập với %s: %s", fieldValue, findValue));
    }
}
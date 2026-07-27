package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends NonRetryableAppException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException(String fieldName, Object value) {
        super(
                HttpStatus.FORBIDDEN,
                String.format("Không có quyền truy cập với %s: %s", fieldName, value)
        );
    }
}
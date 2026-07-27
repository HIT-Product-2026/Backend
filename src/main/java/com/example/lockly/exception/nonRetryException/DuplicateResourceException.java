package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends NonRetryableAppException {

    public DuplicateResourceException(String resourceName, String fieldName, Object value) {
        super(
                HttpStatus.CONFLICT,
                String.format("%s đã tồn tại với %s: %s", resourceName, fieldName, value)
        );
    }

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
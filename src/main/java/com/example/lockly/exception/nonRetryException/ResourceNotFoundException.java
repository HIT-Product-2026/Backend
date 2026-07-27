package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends NonRetryableAppException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object value) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("%s không tìm thấy với %s: %s", resourceName, fieldName, value)
        );
    }
}
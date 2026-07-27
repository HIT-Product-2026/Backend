package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class InvalidCursorException extends NonRetryableAppException {

    public InvalidCursorException() {
        super(HttpStatus.BAD_REQUEST, "Cursor không hợp lệ.");
    }

    public InvalidCursorException(Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "Cursor không hợp lệ.", cause);
    }
}
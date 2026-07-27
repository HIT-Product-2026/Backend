package com.example.lockly.exception.nonRetryException;

import com.example.lockly.exception.AppException;
import org.springframework.http.HttpStatus;

public abstract class NonRetryableAppException extends AppException {

    protected NonRetryableAppException(HttpStatus status, String message, Throwable cause) {
        super(status, message, cause);
    }

    protected NonRetryableAppException(HttpStatus status, String message) {
        super(status, message);
    }
}
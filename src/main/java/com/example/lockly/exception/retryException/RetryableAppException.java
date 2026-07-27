package com.example.lockly.exception.retryException;

import com.example.lockly.exception.AppException;
import org.springframework.http.HttpStatus;

public abstract class RetryableAppException extends AppException {

    protected RetryableAppException(HttpStatus status, String message, Throwable cause) {
        super(status, message, cause);
    }

    protected RetryableAppException(HttpStatus status, String message) {
        super(status, message);
    }
}
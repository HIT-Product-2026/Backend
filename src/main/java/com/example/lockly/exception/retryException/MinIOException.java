package com.example.lockly.exception.retryException;

import org.springframework.http.HttpStatus;

public class MinIOException extends RetryableAppException {

    public MinIOException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, null);
    }

    public MinIOException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
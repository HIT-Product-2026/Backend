package com.example.lockly.exception.retryException;

import org.springframework.http.HttpStatus;

public class QdrantException extends RetryableAppException {

    public QdrantException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, null);
    }

    public QdrantException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
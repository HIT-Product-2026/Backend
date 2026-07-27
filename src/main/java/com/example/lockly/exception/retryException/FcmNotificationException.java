package com.example.lockly.exception.retryException;

import org.springframework.http.HttpStatus;

public class FcmNotificationException extends RetryableAppException {

    public FcmNotificationException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public FcmNotificationException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }
}
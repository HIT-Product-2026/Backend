package com.example.lockly.exception.retryException;

import com.example.lockly.exception.AppException;

public class RetryableAppException extends AppException {

    public RetryableAppException(int errorCode, String message) {
        super(errorCode, message);
    }

    public RetryableAppException(int errorCode, String message, Throwable cause) {
        super(errorCode, message);
        initCause(cause);
    }
}
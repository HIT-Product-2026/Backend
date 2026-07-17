package com.example.lockly.exception.nonRetryException;

import com.example.lockly.exception.AppException;

public class NonRetryableAppException extends AppException {

    public NonRetryableAppException(int errorCode, String message) {
        super(errorCode, message);
    }

    public NonRetryableAppException(int errorCode, String message, Throwable cause) {
        super(errorCode, message);
        initCause(cause);
    }
}
package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class InvalidConfigurationException extends NonRetryableAppException {

    public InvalidConfigurationException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
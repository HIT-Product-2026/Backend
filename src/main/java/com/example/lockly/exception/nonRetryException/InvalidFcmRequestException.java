package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class InvalidFcmRequestException extends NonRetryableAppException {

    public InvalidFcmRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
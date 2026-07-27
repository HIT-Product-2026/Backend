package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends NonRetryableAppException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, message, cause);
    }
}
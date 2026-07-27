package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends NonRetryableAppException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
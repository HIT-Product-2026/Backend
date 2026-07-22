package com.example.lockly.exception.nonRetryException;

public class UnauthorizedException extends NonRetryableAppException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
}

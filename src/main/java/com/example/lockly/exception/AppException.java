package com.example.lockly.exception;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {

    private final HttpStatus status;

    protected AppException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    protected AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

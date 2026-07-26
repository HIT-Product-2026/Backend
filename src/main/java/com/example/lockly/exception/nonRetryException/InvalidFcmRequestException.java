package com.example.lockly.exception.nonRetryException;

public class InvalidFcmRequestException extends NonRetryableAppException {

    public InvalidFcmRequestException(String message) {
        super(400, message);
    }
}

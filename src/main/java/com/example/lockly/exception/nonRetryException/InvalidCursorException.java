package com.example.lockly.exception.nonRetryException;

public class InvalidCursorException extends NonRetryableAppException {

    private static final int ERROR_CODE = 1001;

    public InvalidCursorException() {
        super(ERROR_CODE, "Invalid cursor");
    }

    public InvalidCursorException(Throwable cause) {
        super(ERROR_CODE, "Invalid cursor", cause);
    }
}
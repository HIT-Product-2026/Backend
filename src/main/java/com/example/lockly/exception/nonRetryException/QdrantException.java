package com.example.lockly.exception.nonRetryException;

public class QdrantException extends NonRetryableAppException {

    public QdrantException(String message) {
        super(5001, message);
    }

    public QdrantException(String message, Throwable cause) {
        super(5001, message);
        initCause(cause);
    }
}
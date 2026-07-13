package com.example.lockly.exception;

public class QdrantException extends AppException {

    public QdrantException(String message) {
        super(5001, message);
    }

    public QdrantException(String message, Throwable cause) {
        super(5001, message);
        initCause(cause);
    }
}
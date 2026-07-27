package com.example.lockly.exception.nonRetryException;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InternalServerException extends NonRetryableAppException {

    private final String[] params;

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        this.params = null;
    }

    public InternalServerException(String message, String[] params) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        this.params = params;
    }
}
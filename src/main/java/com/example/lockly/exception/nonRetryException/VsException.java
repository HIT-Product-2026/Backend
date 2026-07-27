package com.example.lockly.exception.nonRetryException;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VsException extends NonRetryableAppException {

    private final Object errMessage;
    private final String[] params;

    public VsException(HttpStatus status, Object errMessage) {
        super(status, String.valueOf(errMessage));
        this.errMessage = errMessage;
        this.params = null;
    }
}
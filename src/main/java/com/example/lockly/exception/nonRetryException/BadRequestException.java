package com.example.lockly.exception.nonRetryException;

import org.springframework.http.HttpStatus;

public class BadRequestException extends NonRetryableAppException {

    public BadRequestException(String fieldValue, Object findValue) {
        super(
                HttpStatus.BAD_REQUEST,
                String.format("Giá trị không hợp lệ với %s: %s", fieldValue, findValue)
        );
    }

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
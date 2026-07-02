package com.example.lockly.exception;

public class BadRequestException extends AppException{
    public BadRequestException( String fieldValue, Object findValue){
        super(400, String.format("Giá trị không hợp lệ với %s: %s", fieldValue, findValue));
    }
    public BadRequestException(String massage){
        super(400, massage);
    }
}

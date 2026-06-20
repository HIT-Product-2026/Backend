package com.example.lockly.exception;

public class DuplicateResourceException extends AppException{
    public DuplicateResourceException(String resourceName, String fieldValue, Object findValue){
        super(409, String.format("%s tìm thấy giá trị trùng với %s: %s", resourceName, fieldValue, findValue));
    }
    public DuplicateResourceException(String message){
        super(409, message);
    }
}

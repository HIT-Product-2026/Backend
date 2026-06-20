package com.example.lockly.exception;

public class DuplicateResourceException extends AppException{
    public DuplicateResourceException(String resourceName, String fieldValue, Object findValue){
        super(409, String.format("%s tìm thấy giá trị trùng với %s: %s", resourceName, fieldValue, findValue));
    }
<<<<<<< HEAD
    public DuplicateResourceException(String message){
        super(409, message);
    }
=======
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
}

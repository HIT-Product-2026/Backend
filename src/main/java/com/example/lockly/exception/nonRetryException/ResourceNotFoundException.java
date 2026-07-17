package com.example.lockly.exception.nonRetryException;

public class ResourceNotFoundException extends NonRetryableAppException {
    public ResourceNotFoundException(String resourceName, String fieldValue, Object findValue){
        super(404, String.format("%s không tìm thấy với %s: %s", resourceName, fieldValue, findValue));
    }
}

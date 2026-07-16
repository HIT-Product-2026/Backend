package com.example.lockly.exception.nonRetryException;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class VsException extends NonRetryableAppException {

    // Mã lỗi mặc định cho VsException (bạn có thể thay đổi số này theo hệ thống của bạn)
    private static final int DEFAULT_ERROR_CODE = 40000;

    private Object errMessage;
    private HttpStatus status;
    private String[] params;

    // 1. Constructor nhận errMessage dạng String (HTTP Status mặc định là 500)
    public VsException(String errMessage) {
        super(DEFAULT_ERROR_CODE, errMessage);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errMessage = errMessage;
    }

    // 2. Constructor nhận HttpStatus và errMessage dạng Object (Map, List, DTO...)
    public VsException(HttpStatus status, Object errMessage) {
        // Chuyển errMessage sang String để truyền vào constructor cha (RuntimeException)
        super(DEFAULT_ERROR_CODE, String.valueOf(errMessage));
        this.status = status;
        this.errMessage = errMessage;
    }

    // 3. Constructor nhận errMessage và params (HTTP Status mặc định là 500)
    public VsException(String errMessage, String[] params) {
        super(DEFAULT_ERROR_CODE, errMessage);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errMessage = errMessage;
        this.params = params;
    }

    // 4. Constructor nhận HttpStatus, errMessage dạng String và params
    public VsException(HttpStatus status, String errMessage, String[] params) {
        super(DEFAULT_ERROR_CODE, errMessage);
        this.status = status;
        this.errMessage = errMessage;
        this.params = params;
    }

    // 5. Constructor mở rộng: Cho phép truyền đầy đủ Custom Error Code nếu cần
    public VsException(int errorCode, HttpStatus status, Object errMessage, String[] params) {
        super(errorCode, String.valueOf(errMessage));
        this.status = status;
        this.errMessage = errMessage;
        this.params = params;
    }
}
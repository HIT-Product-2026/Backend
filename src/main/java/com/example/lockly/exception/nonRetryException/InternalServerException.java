package com.example.lockly.exception.nonRetryException;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class InternalServerException extends NonRetryableAppException {

    // Định nghĩa một errorCode mặc định cho lỗi hệ thống (ví dụ: 50000 hoặc tùy bạn chọn)
    private static final int DEFAULT_ERROR_CODE = 50000;

    private HttpStatus status;
    private String[] params;

    // constructor 1: Chỉ truyền message (sử dụng errorCode mặc định và HTTP Status 500)
    public InternalServerException(String message) {
        super(DEFAULT_ERROR_CODE, message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // constructor 2: Truyền status và message
    public InternalServerException(HttpStatus status, String message) {
        super(DEFAULT_ERROR_CODE, message);
        this.status = status;
    }

    // constructor 3: Truyền message và params
    public InternalServerException(String message, String[] params) {
        super(DEFAULT_ERROR_CODE, message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.params = params;
    }

    // constructor 4: Truyền đầy đủ status, message và params
    public InternalServerException(HttpStatus status, String message, String[] params) {
        super(DEFAULT_ERROR_CODE, message);
        this.status = status;
        this.params = params;
    }

    // constructor 5 (Mở rộng): Cho phép truyền cả custom errorCode nếu cần thiết
    public InternalServerException(int errorCode, HttpStatus status, String message, String[] params) {
        super(errorCode, message);
        this.status = status;
        this.params = params;
    }
}
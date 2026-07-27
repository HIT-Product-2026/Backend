package com.example.lockly.exception;

import com.example.lockly.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {

        // Log toàn bộ stack trace nếu có cause
        log.error("Application exception", ex);

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(
                        ex.getStatus().value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationError(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String code = error.getCode();

            boolean isEmptyCheck =
                    "NotBlank".equals(code) || "NotNull".equals(code);

            if (!errors.containsKey(field) || isEmptyCheck) {
                errors.put(field, error.getDefaultMessage());
            }
        });

        String combinedMessage = String.join(", ", errors.values());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, combinedMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {

        log.error("Unexpected exception", ex);

        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(
                        500,
                        "Đã xảy ra lỗi hệ thống."
                ));
    }
}
package com.example.lockly.exception;

<<<<<<< HEAD
import com.example.lockly.common.response.ApiResponse;
=======
import com.example.lockly.common.ApiResponse;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.HashMap;
=======
import java.util.LinkedHashMap;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandle {
<<<<<<< HEAD
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex){
=======

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(DuplicateResourceException ex){
=======
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(DuplicateResourceException ex) {
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex){
=======
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

<<<<<<< HEAD
    //====================================== Validation ==================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handValidationError(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<Map<String, String>> response = new ApiResponse<>(400, "Dữ liệu không hợp lệ", errors, LocalDateTime.now());

        return ResponseEntity.badRequest().body(response);
    }

    //=============== Exception chung =====================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handGeneriException(Exception ex){
=======
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String code = error.getCode(); // tên annotation: NotBlank, NotNull, Size, Pattern...
            boolean isEmptyCheck = "NotBlank".equals(code) || "NotNull".equals(code);

            if (!errors.containsKey(field) || isEmptyCheck) {
                errors.put(field, error.getDefaultMessage());
            }
        });

        String combinedMessage = String.join(", ", errors.values());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, combinedMessage));
    }

    // Exception chung
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handGenericException(Exception ex) {
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Lỗi hệ thống, vui lòng thử lại sau"));
    }
<<<<<<< HEAD

}
=======
}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

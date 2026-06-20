package com.example.lockly.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@NotBlank(message = "Email không được để trống")
@Pattern(
        regexp = "^$|^[a-z0-9.]{6,30}@[a-z0-9]+\\.[a-z]{2,}$",
        message = "Email không hợp lệ. Phần tên tài khoản chỉ dùng chữ thường (a-z), số (0-9), dấu chấm (.) và từ 6 đến 30 ký tự"
)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "Email không hợp lệ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
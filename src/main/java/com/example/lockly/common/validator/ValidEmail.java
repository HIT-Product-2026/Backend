package com.example.lockly.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@NotBlank(message = "Email không được để trống")
@Pattern(
        regexp = "^[a-zA-Z0-9._%+\\-]+@gmail\\.com$",
        message = "Email sai định dạng."
)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "Email sai định dạng.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
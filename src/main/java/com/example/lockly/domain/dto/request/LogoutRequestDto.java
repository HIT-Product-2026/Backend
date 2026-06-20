package com.example.lockly.domain.dto.request;

<<<<<<< HEAD
import com.example.lockly.constant.ErrorMessage;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(
        @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
        String token
) {}
=======
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(

        @NotBlank(message = "Token không được để trống")
        String token

) {}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

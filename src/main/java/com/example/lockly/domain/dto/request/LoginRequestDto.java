package com.example.lockly.domain.dto.request;

<<<<<<< HEAD
public record LoginRequestDto(
        String username,
        String password,
        String email
) {
}

=======
import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;

public record LoginRequestDto(

        @ValidEmail
        String email,

        @ValidPassword
        String password

) {}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

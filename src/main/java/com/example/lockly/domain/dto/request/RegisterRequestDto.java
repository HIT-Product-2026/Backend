package com.example.lockly.domain.dto.request;

<<<<<<< HEAD

public record RegisterRequestDto(
    String username,
    String email,
    String password
){

}
=======
import com.example.lockly.common.validator.ValidEmail;

public record RegisterRequestDto(

        @ValidEmail
        String email

) {}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

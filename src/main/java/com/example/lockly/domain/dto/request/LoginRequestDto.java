package com.example.lockly.domain.dto.request;

import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;

public record LoginRequestDto(

        @ValidEmail
        String email,

        @ValidPassword
        String password

) {}
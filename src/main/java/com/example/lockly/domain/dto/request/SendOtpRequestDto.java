package com.example.lockly.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendOtpRequestDto(

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$",
                message = "Số điện thoại không đúng định dạng Việt Nam")
        String phoneNumber

) {}
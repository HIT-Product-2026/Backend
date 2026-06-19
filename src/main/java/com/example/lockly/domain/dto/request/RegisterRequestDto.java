package com.example.lockly.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(

        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3 đến 50 ký tự")
        String username,

        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(min = 2, max = 100, message = "Tên hiển thị từ 2 đến 100 ký tự")
        String displayName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu từ 6 đến 100 ký tự")
        String password

) {}
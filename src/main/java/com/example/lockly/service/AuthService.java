package com.example.lockly.service;

import com.example.lockly.domain.dto.request.LoginRequestDto;
import com.example.lockly.domain.dto.request.LogoutRequestDto;
import com.example.lockly.domain.dto.request.RegisterRequestDto;
import com.example.lockly.domain.dto.response.CommonResponseDto;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;

public interface AuthService {

    public UserResponseDto register(RegisterRequestDto request);
    public LoginResponseDto authentication(LoginRequestDto request);
    public CommonResponseDto logout(LogoutRequestDto request);
}

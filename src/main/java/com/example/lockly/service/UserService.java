package com.example.lockly.service;

import com.example.lockly.domain.dto.response.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserResponseDto getMyInfo(String username);
}
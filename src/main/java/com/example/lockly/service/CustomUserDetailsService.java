package com.example.lockly.service;

import com.example.lockly.domain.dto.response.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {
    public UserDetails loadUserByUsername(String username);
    UserResponseDto getMyInfo(String username);
}
package com.example.lockly.service;

import org.springframework.security.core.userdetails.UserDetailsService;

// FIX: extend UserDetailsService để Spring nhận diện được Bean
public interface UserService extends UserDetailsService {
}
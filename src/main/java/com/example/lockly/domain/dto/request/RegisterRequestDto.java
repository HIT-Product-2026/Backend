package com.example.lockly.domain.dto.request;


public record RegisterRequestDto(
    String username,
    String email,
    String password
){

}

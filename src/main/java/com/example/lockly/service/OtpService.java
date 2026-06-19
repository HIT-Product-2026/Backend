package com.example.lockly.service;

import com.example.lockly.domain.entity.OtpPurpose;

public interface OtpService {
    void sendOtp(String email, OtpPurpose purpose);
    void verifyOtp(String email, String inputOtp, OtpPurpose purpose);
}
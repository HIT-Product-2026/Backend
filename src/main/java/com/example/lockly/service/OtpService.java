package com.example.lockly.service;

import com.example.lockly.domain.entity.OtpPurpose;

public interface OtpService {
    void sendOtp(String phoneNumber, OtpPurpose purpose);
    void verifyOtp(String phoneNumber, String inputOtp, OtpPurpose purpose);
}
package com.example.lockly.service;
public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode, String purpose);
}
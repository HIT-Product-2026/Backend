package com.example.lockly.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder ENCODER =
            new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    // Hash password
    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

     // Verify password
    public static boolean verify(String rawPassword, String hashedPassword) {
        return ENCODER.matches(rawPassword, hashedPassword);
    }
}

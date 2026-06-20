package com.example.lockly.common.util;

<<<<<<< HEAD
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
=======
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder;

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean verify(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}   
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

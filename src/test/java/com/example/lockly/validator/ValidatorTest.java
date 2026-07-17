package com.example.lockly.validator;

import com.example.lockly.exception.nonRetryException.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Validator Tests")
class ValidatorTest {

    @Autowired
    private YourValidatorClass validator;

    @Test
    @DisplayName("Should validate email format")
    void testValidateEmail_Valid() {
        // Act & Assert
        assertTrue(validator.isValidEmail("test@example.com"));
        assertTrue(validator.isValidEmail("user.name@domain.co.uk"));
    }

    @Test
    @DisplayName("Should reject invalid email")
    void testValidateEmail_Invalid() {
        // Act & Assert
        assertFalse(validator.isValidEmail("invalid.email"));
        assertFalse(validator.isValidEmail("@example.com"));
        assertFalse(validator.isValidEmail("user@"));
    }

    @Test
    @DisplayName("Should validate password strength")
    void testValidatePassword_Strong() {
        // Act & Assert
        assertTrue(validator.isStrongPassword("SecureP@ss123"));
        assertTrue(validator.isStrongPassword("MyPassword@2024"));
    }

    @Test
    @DisplayName("Should reject weak password")
    void testValidatePassword_Weak() {
        // Act & Assert
        assertFalse(validator.isStrongPassword("weak"));
        assertFalse(validator.isStrongPassword("123456"));
        assertFalse(validator.isStrongPassword("password"));
    }

    @Test
    @DisplayName("Should validate location coordinates")
    void testValidateCoordinates_Valid() {
        // Act & Assert
        assertTrue(validator.isValidCoordinates(10.7769, 106.7009));
        assertTrue(validator.isValidCoordinates(0.0, 0.0));
    }

    @Test
    @DisplayName("Should reject invalid coordinates")
    void testValidateCoordinates_Invalid() {
        // Act & Assert
        assertFalse(validator.isValidCoordinates(91.0, 0.0)); // Latitude out of range
        assertFalse(validator.isValidCoordinates(0.0, 181.0)); // Longitude out of range
    }

    @Test
    @DisplayName("Should validate UUID format")
    void testValidateUUID_Valid() {
        // Act & Assert
        assertTrue(validator.isValidUUID("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    @DisplayName("Should reject invalid UUID")
    void testValidateUUID_Invalid() {
        // Act & Assert
        assertFalse(validator.isValidUUID("not-a-uuid"));
        assertFalse(validator.isValidUUID("123"));
    }

    @Test
    @DisplayName("Should validate caption length")
    void testValidateCaption_Valid() {
        // Act & Assert
        assertTrue(validator.isValidCaption("This is a valid caption"));
    }

    @Test
    @DisplayName("Should reject caption that's too long")
    void testValidateCaption_TooLong() {
        // Act & Assert
        String longCaption = "a".repeat(3000);
        assertFalse(validator.isValidCaption(longCaption));
    }

    @Test
    @DisplayName("Should validate display name")
    void testValidateDisplayName_Valid() {
        // Act & Assert
        assertTrue(validator.isValidDisplayName("John Doe"));
        assertTrue(validator.isValidDisplayName("用户123"));
    }

    @Test
    @DisplayName("Should reject invalid display name")
    void testValidateDisplayName_Invalid() {
        // Act & Assert
        assertFalse(validator.isValidDisplayName(""));
        assertFalse(validator.isValidDisplayName("a".repeat(200)));
    }
}

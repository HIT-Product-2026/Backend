package com.example.lockly.exception;

import com.example.lockly.exception.nonRetryException.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Exception Handler Tests")
class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void notFoundException() {
            throw new NotFoundException("Resource not found");
        }

        @GetMapping("/test/bad-request")
        public void badRequestException() {
            throw new BadRequestException("Invalid request");
        }

        @GetMapping("/test/forbidden")
        public void forbiddenException() {
            throw new ForbiddenException("Access forbidden");
        }

        @GetMapping("/test/unauthorized")
        public void unauthorizedException() {
            throw new UnauthorizedException("Unauthorized access");
        }

        @GetMapping("/test/conflict")
        public void conflictException() {
            throw new ConflictException("Resource already exists");
        }
    }

    @Test
    @DisplayName("Should handle NotFoundException with 404 status")
    void testNotFoundExceptionHandler() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    @DisplayName("Should handle BadRequestException with 400 status")
    void testBadRequestExceptionHandler() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("Should handle ForbiddenException with 403 status")
    void testForbiddenExceptionHandler() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("403"));
    }

    @Test
    @DisplayName("Should handle UnauthorizedException with 401 status")
    void testUnauthorizedExceptionHandler() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @DisplayName("Should handle ConflictException with 409 status")
    void testConflictExceptionHandler() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("409"));
    }
}

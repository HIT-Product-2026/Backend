package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MinIOService Unit Tests")
class MinIOServiceTest {

    @Autowired
    private MinIOService minIOService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .build();
    }

    @Test
    @DisplayName("Should upload image file successfully")
    void testUploadImage_Success() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test-image.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");

        // Act
        String uploadUrl = minIOService.uploadImage(file, testUserId);

        // Assert
        assertNotNull(uploadUrl);
        assertTrue(uploadUrl.contains("lockly"));
    }

    @Test
    @DisplayName("Should validate image format")
    void testValidateImageFormat_Valid() {
        // Act & Assert
        assertTrue(minIOService.isValidImageFormat("image.jpg"));
        assertTrue(minIOService.isValidImageFormat("image.png"));
        assertTrue(minIOService.isValidImageFormat("image.jpeg"));
    }

    @Test
    @DisplayName("Should reject invalid image format")
    void testValidateImageFormat_Invalid() {
        // Act & Assert
        assertFalse(minIOService.isValidImageFormat("image.pdf"));
        assertFalse(minIOService.isValidImageFormat("image.txt"));
        assertFalse(minIOService.isValidImageFormat("document.doc"));
    }

    @Test
    @DisplayName("Should download image successfully")
    void testDownloadImage_Success() {
        // Arrange
        UUID imageId = UUID.randomUUID();
        String testImageData = "fake image data";
        InputStream inputStream = new ByteArrayInputStream(testImageData.getBytes());

        // Act
        assertDoesNotThrow(() -> {
            InputStream result = minIOService.downloadImage(imageId);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Should delete image successfully")
    void testDeleteImage_Success() {
        // Arrange
        UUID imageId = UUID.randomUUID();

        // Act
        assertDoesNotThrow(() -> minIOService.deleteImage(imageId));
    }

    @Test
    @DisplayName("Should check image file size")
    void testCheckImageFileSize() {
        // Arrange
        long maxSize = 10 * 1024 * 1024; // 10MB

        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(5 * 1024 * 1024); // 5MB

        // Act
        boolean isValid = minIOService.isValidImageSize(file);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject oversized image")
    void testRejectOversizedImage() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(100 * 1024 * 1024); // 100MB

        // Act
        boolean isValid = minIOService.isValidImageSize(file);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate unique image URL")
    void testGenerateImageUrl() {
        // Arrange
        UUID imageId = UUID.randomUUID();

        // Act
        String url = minIOService.generateImageUrl(imageId);

        // Assert
        assertNotNull(url);
        assertTrue(url.contains(imageId.toString()));
    }

    @Test
    @DisplayName("Should batch upload multiple images")
    void testBatchUploadImages() {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);

        when(file1.getOriginalFilename()).thenReturn("image1.jpg");
        when(file2.getOriginalFilename()).thenReturn("image2.jpg");

        // Act
        assertDoesNotThrow(() -> minIOService.uploadImages(new MultipartFile[]{file1, file2}, testUserId));
    }
}

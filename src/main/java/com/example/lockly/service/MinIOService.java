package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface MinIOService {
    void saveFile(MultipartFile file, String objectName) throws Exception;
    InputStream getFile(String objectName);
    MultipartFile getMultipartFile(String objectName);
    void deleteFile(String objectName);
    String generatePresignedUrl(String objectName) throws Exception;
}

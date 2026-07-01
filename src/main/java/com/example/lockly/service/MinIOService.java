package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinIOService {
    void saveFile(MultipartFile file, String objectName) throws Exception;
    InputStream getFile(String objectName) throws Exception;
    void deleteFile(String objectName);
}

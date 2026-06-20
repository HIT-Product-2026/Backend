package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinIOService {
    public void saveFile(MultipartFile file, String objectName) throws Exception;
    public InputStream getFile(String objectName) throws Exception;
    public void deleteFile(String objectName);
}

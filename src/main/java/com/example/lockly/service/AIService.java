package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AIService {
    public Boolean detectNsfw(MultipartFile imageFile) throws IOException;
}

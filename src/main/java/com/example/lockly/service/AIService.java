package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface AIService {

    Boolean detectNsfw(MultipartFile imageFile) throws IOException;

    List<String> detectFaces(MultipartFile imageFile) throws IOException;

    Boolean registerFace(UUID personId, MultipartFile imageFile);

    Boolean checkFace(UUID personId);
}
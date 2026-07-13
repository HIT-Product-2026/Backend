package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface AIService {

    Boolean detectNsfw(String objectName);

    List<UUID> detectFaces(String objectName);

    Boolean registerFace(UUID userId, String objectName);

    Boolean checkFace(UUID personId);
}
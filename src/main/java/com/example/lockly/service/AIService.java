package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface AIService {

    Boolean detectNsfw(String bucket, String objectName) throws IOException;

    List<String> detectFaces(String bucket, String objectName) throws IOException;

    Boolean registerFace(UUID personId, String bucket, String objectName);

    Boolean checkFace(UUID personId);
}
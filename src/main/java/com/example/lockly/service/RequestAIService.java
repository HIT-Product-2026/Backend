package com.example.lockly.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface RequestAIService {

    Boolean detectNsfw(MultipartFile file);

    List<List<Float>> detectFaces(MultipartFile file);
}

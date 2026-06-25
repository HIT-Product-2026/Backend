package com.example.lockly.service.Impl;

import com.example.lockly.service.AIService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AIServiceImpl implements AIService {

    private static final String AI_API_URL = "http://localhost:8000/detect";

    public Boolean detectNsfw(MultipartFile imageFile) throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(
                "file",
                new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                    }
                }
        );

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Boolean> response =
                restTemplate.postForEntity(
                        AI_API_URL,
                        request,
                        Boolean.class
                );

        return response.getBody();
    }
}
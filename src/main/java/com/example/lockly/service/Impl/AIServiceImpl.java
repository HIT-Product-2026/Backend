package com.example.lockly.service.Impl;

import com.example.lockly.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class AIServiceImpl implements AIService {

    private static final String AI_API_URL = "http://localhost:8000/detect";

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "fallback"
    )
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

    public Boolean fallback(
            MultipartFile imageFile,
            Exception ex) {

        log.debug("AI unavailable: " + ex.getMessage());

        return false;
    }
}
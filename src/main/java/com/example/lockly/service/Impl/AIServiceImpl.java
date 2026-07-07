package com.example.lockly.service.Impl;

import com.example.lockly.service.AIService;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private static final String AI_API_URL = "http://localhost:8000/detect";
    private static final String FACE_REGISTER_API = "http://localhost:8000/face/register";
    private static final String FACE_DETECT_API = "http://localhost:8000/face/detect";
    private static final String FACE_CHECK_API = "http://localhost:8000/face/check";

    private final RestTemplate restTemplate;

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "fallback"
    )
    public Boolean detectNsfw(MultipartFile imageFile) throws IOException {

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

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "detectFaceFallback"
    )
    public List<String> detectFaces(MultipartFile imageFile) throws IOException {

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

        ResponseEntity<List> response =
                restTemplate.postForEntity(
                        FACE_DETECT_API,
                        request,
                        List.class
                );

        return response.getBody();
    }

    public List<String> detectFaceFallback(
            MultipartFile imageFile,
            Exception ex
    ) {

        log.error("Face detect failed", ex);

        return Collections.emptyList();
    }


    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "registerFallback"
    )
    public Boolean registerFace(
            UUID personId,
            MultipartFile imageFile
    ) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("person_id", personId.toString());

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
                            FACE_REGISTER_API,
                            request,
                            Boolean.class
                    );

            return Boolean.TRUE.equals(response.getBody());

        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file ảnh", e);
        }
    }

    public Boolean registerFallback(
            UUID personId,
            MultipartFile imageFile,
            Exception ex
    ) {

        log.error("Register face failed", ex);

        return false;
    }

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "checkFaceFallback"
    )
    public Boolean checkFace(UUID personId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("person_id", personId.toString());

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Boolean> response =
                restTemplate.postForEntity(
                        FACE_CHECK_API,
                        request,
                        Boolean.class
                );

        return response.getBody();
    }

    public Boolean checkFaceFallback(
            UUID personId,
            Exception ex
    ) {

        log.error("Check face failed", ex);

        return false;
    }
}
package com.example.lockly.service.Impl;

import com.example.lockly.service.AIService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
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

    private static final String BASE_URL = "http://localhost:8000";

    private static final String AI_API_URL = BASE_URL + "/detect";
    private static final String FACE_REGISTER_API = BASE_URL + "/face/register";
    private static final String FACE_DETECT_API = BASE_URL + "/face/detect";
    private static final String FACE_CHECK_API = BASE_URL + "/face/check";

    private final RestTemplate restTemplate;


    // Build multipart/form-data request
    private HttpEntity<MultiValueMap<String, Object>> buildMultipartRequest(
            MultipartFile imageFile,
            Map<String, String> extraFields
    ) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        if (extraFields != null) {
            extraFields.forEach(body::add);
        }

        body.add(
                "file",
                new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                    }
                }
        );

        return new HttpEntity<>(body, headers);
    }

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "fallback"
    )
    public Boolean detectNsfw(String bucket, String objectName) throws IOException {
        return false;

//        HttpEntity<MultiValueMap<String, Object>> request =
//                buildMultipartRequest(imageFile, null);
//
//        ResponseEntity<Boolean> response =
//                restTemplate.postForEntity(
//                        AI_API_URL,
//                        request,
//                        Boolean.class
//                );
//
//        return Boolean.TRUE.equals(response.getBody());
    }

    public Boolean fallback(
            MultipartFile imageFile,
            Exception ex
    ) {

        log.warn("AI unavailable: {}", ex.getMessage());

        return false;
    }

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "detectFaceFallback"
    )
    public List<String> detectFaces(String bucket, String objectName) throws IOException {
        return null;

//        HttpEntity<MultiValueMap<String, Object>> request =
//                buildMultipartRequest(imageFile, null);
//
//        ResponseEntity<List<String>> response =
//                restTemplate.exchange(
//                        FACE_DETECT_API,
//                        HttpMethod.POST,
//                        request,
//                        new ParameterizedTypeReference<>() {}
//                );
//
//        return response.getBody();
    }

    public List<String> detectFaceFallback(
            MultipartFile imageFile,
            Exception ex
    ) {

        log.warn("Face detect failed", ex);

        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "registerFallback"
    )
    public Boolean registerFace(
            UUID personId,
            String bucket,
            String objectName
    ) {
        return false;
//
//        try {
//
//            Map<String, String> fields = new HashMap<>();
//            fields.put("person_id", personId.toString());
//
//            HttpEntity<MultiValueMap<String, Object>> request =
//                    buildMultipartRequest(imageFile, fields);
//
//            ResponseEntity<Boolean> response =
//                    restTemplate.postForEntity(
//                            FACE_REGISTER_API,
//                            request,
//                            Boolean.class
//                    );
//
//            return Boolean.TRUE.equals(response.getBody());
//
//        } catch (IOException e) {
//
//            throw new RuntimeException(
//                    "Không thể đọc file ảnh",
//                    e
//            );
//        }
    }

    public Boolean registerFallback(
            UUID personId,
            MultipartFile imageFile,
            Exception ex
    ) {

        log.warn("Register face failed", ex);

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

        return Boolean.TRUE.equals(response.getBody());
    }

    public Boolean checkFaceFallback(
            UUID personId,
            Exception ex
    ) {

        log.warn("Check face failed", ex);

        return false;
    }
}
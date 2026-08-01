package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.NsfwResponse;
import com.example.lockly.service.RequestAIService;
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
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestAIServiceImpl implements RequestAIService {

    private static final String BASE_URL = "http://lockly-ai:8000";

    private static final String NSFW_API = BASE_URL + "/nsfw/detect";
    private static final String FACE_DETECT_API = BASE_URL + "/face/detect";

    private final RestTemplate restTemplate;

    private HttpEntity<MultiValueMap<String, Object>> buildMultipartRequest(
            MultipartFile file
    ) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        return new HttpEntity<>(body, headers);
    }

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "detectNsfwFallback"
    )
    public Boolean detectNsfw(MultipartFile file) {

        try {
            HttpEntity<MultiValueMap<String, Object>> request =
                    buildMultipartRequest(file);

            ResponseEntity<NsfwResponse> response =
                    restTemplate.postForEntity(
                            NSFW_API,
                            request,
                            NsfwResponse.class
                    );

            NsfwResponse body = response.getBody();

            if (body == null) {
                return false;
            }

            log.info(
                    "NSFW detect result={}, score={}",
                    body.nsfw(),
                    body.score()
            );

            return Boolean.TRUE.equals(body.nsfw());

        } catch (Exception e) {
            log.error("NSFW detect error", e);
            return false;
        }
    }

    @Override
    @CircuitBreaker(
            name = "aiService",
            fallbackMethod = "detectFacesFallback"
    )
    public List<List<Float>> detectFaces(MultipartFile file) {

        try {
            HttpEntity<MultiValueMap<String, Object>> request =
                    buildMultipartRequest(file);

            ResponseEntity<List<List<Float>>> response =
                    restTemplate.exchange(
                            FACE_DETECT_API,
                            HttpMethod.POST,
                            request,
                            new ParameterizedTypeReference<>() {}
                    );

            return response.getBody();

        } catch (Exception e) {
            log.error("Face detect error", e);
            return Collections.emptyList();
        }
    }

    public Boolean detectNsfwFallback(
            MultipartFile file,
            Exception ex
    ) {
        log.warn("NSFW detect failed", ex);
        return false;
    }

    public List<List<Float>> detectFacesFallback(
            MultipartFile file,
            Exception ex
    ) {
        log.warn("Face detect failed", ex);
        return Collections.emptyList();
    }
}
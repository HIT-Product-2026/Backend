package com.example.lockly.controller;

import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.entity.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_V1 + "/sse")
@Tag(name = "Sse Controller", description = "Dùng để quản lý Sse")
public class SseController {

    private final SseService sseService;
    private final AuthService authService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Mở SSE connection")
    public SseEmitter subscribe() {

        User user = authService.getCurrentUser();

        return sseService.subscribeDetectNsfw(user.getId());
    }
}

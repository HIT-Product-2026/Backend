package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.create.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_NOW + "/message")
@Tag(name = "Message Controller", description = "API quản lý tin nhắn")
public class MessageController {

    private final MessageService messageService;
    private final AuthService authService;

    @PostMapping("/text")
    @Operation(summary = "Gửi text message", description = "Gửi tin nhắn văn bản")
    public ResponseEntity<ApiResponse<MessageResponseDto>> sendTextMessage(
            @RequestBody
            SendTextMessageRequestDto request
    ) {

        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                                "Gửi tin nhắn thành công",
                                messageService.sendTextMessage(request, user)
                        )
                );
    }


    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Gửi image message", description = "Upload ảnh để gửi tin nhắn")
    public ResponseEntity<ApiResponse<MessageResponseDto>> sendImageMessage(
            @Parameter(description = "ID conversation")
            @RequestParam("conversationId")
            UUID conversationId,

            @Parameter(description = "File ảnh")
            @RequestParam("file")
            MultipartFile file

    ){

        SendImageMessageRequestDto request = new SendImageMessageRequestDto(conversationId, file);

        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Gửi ảnh thành công", messageService.sendImageMessage(request, user)));
    }

    @GetMapping("/{message_id}")
    @Operation(summary = "Lấy message theo ID", description = "Trả về thông tin message")
    public ResponseEntity<ApiResponse<MessageResponseDto>> findMessageById(
            @Parameter(description = "ID message")
            @PathVariable("message_id")
            UUID messageId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", messageService.findMessageById(messageId)));
    }


    @GetMapping("/{message_id}/image")
    @Operation(summary = "Lấy ảnh message", description = "Trả về ảnh binary từ MinIO")
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "ID message")
            @PathVariable("message_id")
            UUID messageId

    ) throws Exception {
        InputStream inputStream = messageService.findImageMessageById(messageId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(inputStream.readAllBytes());
    }
}
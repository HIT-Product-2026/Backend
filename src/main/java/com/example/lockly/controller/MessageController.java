package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.MessageResponseDto;
import com.example.lockly.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(ApiPath.API_V1 + "/messages")
@Tag(name = "Message Controller", description = "API quản lý tin nhắn")
public class MessageController {

    MessageService messageService;

    @PostMapping("/send-text")
    @Operation(summary = "Gửi text message", description = "Gửi tin nhắn văn bản")
    public ResponseEntity<ApiResponse<MessageResponseDto>> sendTextMessage(
            @RequestBody
            SendTextMessageRequestDto request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                                "Gửi tin nhắn thành công",
                                messageService.sendTextMessage(request)
                        )
                );
    }


    @PostMapping(value = "/send-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Gửi image message", description = "Upload ảnh để gửi tin nhắn")
    public ResponseEntity<ApiResponse<MessageResponseDto>> sendImageMessage(
            @Parameter(description = "ID conversation")
            @RequestParam("conversationId")
            String conversationId,

            @Parameter(description = "File ảnh")
            @RequestParam("file")
            MultipartFile file

    ) throws Exception {
        SendImageMessageRequestDto request = new SendImageMessageRequestDto(conversationId, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Gửi ảnh thành công", messageService.sendImageMessage(request)));
    }


    @GetMapping("/{conversation_id}/messages")
    @Operation(summary = "Lấy danh sách message", description = "Lấy tất cả message theo conversation")
    public ResponseEntity<ApiResponse<List<MessageResponseDto>>> findMessagesByConversationId(
            @Parameter(description = "ID conversation")
            @PathVariable("conversation_id")
            String conversationId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Thành công",
                        messageService.findMessagesByConversationId(conversationId)));
    }


    @GetMapping("/{message_id}")
    @Operation(summary = "Lấy message theo ID", description = "Trả về thông tin message")
    public ResponseEntity<ApiResponse<MessageResponseDto>> findMessageById(
            @Parameter(description = "ID message")
            @PathVariable("message_id")
            String messageId
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
            String messageId

    ) throws Exception {
        InputStream inputStream = messageService.findImageMessageById(messageId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(inputStream.readAllBytes());
    }
}
package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(ApiPath.API_V1 + "/conversations")
@Tag(name = "Conversation Controller", description = "API quản lý hội thoại")
public class ConversationController {

    ConversationService conversationService;

    @GetMapping("/{conversation_id}")
    @Operation(summary = "Lấy conversation theo id")
    public ResponseEntity<ApiResponse<ConversationResponseDto>> getConversationById(
            @PathVariable("conversation_id") UUID conversationId
    ) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", conversationService.findById(conversationId)));
    }


    @PostMapping
    @Operation(summary = "Tạo conversation mới")
    public ResponseEntity<ApiResponse<ConversationResponseDto>> createConversation(
            @RequestBody CreateConversationRequestDto request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Tạo conversation thành công",
                        conversationService.createConversation(request)
                ));
    }
}
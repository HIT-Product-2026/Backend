package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.create.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationRealtimeResponseDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.N;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_NOW + "/message")
@Tag(name = "Message Controller", description = "API quản lý tin nhắn")
public class MessageController {

    private final MessageService messageService;
    private final AuthService authService;
    private final WebSocketService webSocketService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final NotificationService notificationService;

//    @PostMapping("/text")
//    @Operation(summary = "Gửi text message", description = "Gửi tin nhắn văn bản")
//    public ResponseEntity<ApiResponse<MessageResponseDto>> sendTextMessage(
//            @RequestBody
//            SendTextMessageRequestDto request
//    ) {
//
//        User user = authService.getCurrentUser();
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ApiResponse.created(
//                                "Gửi tin nhắn thành công",
//                                messageService.sendTextMessage(request, user)
//                        )
//                );
//    }


    @PostMapping(value = "/image")
    @Operation(summary = "Gửi image message", description = "Upload ảnh để gửi tin nhắn")
    public ResponseEntity<ApiResponse<MessageResponseDto>> sendImageMessage(
            @RequestBody
            SendImageMessageRequestDto request
    ){

        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        // Lưu vào DB
        MessageResponseDto response = messageService.sendImageMessage(request, user);

        // Gửi tin nhắn vào màn hình chat
        webSocketService.sendImageMessage(
                request.conversationId(),
                response
        );

        // Lấy username của người còn lại
        User userOther = conversationService.getOtherUser(
                request.conversationId(),
                user.getId()
        );

        // Lấy conversation đã fetch user1, user2
        Conversation conversation = conversationRepository
                .findByIdWithUsers(response.conversationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation",
                        "conversation id",
                        response.conversationId()
                ));

        ConversationRealtimeResponseDto dto =
                ConversationRealtimeResponseDto.from(conversation);

        // Gửi cập nhật danh sách conversation cho người nhận
        webSocketService.pubMessageToConversations(
                userOther.getUsername(),
                dto
        );

        // Gửi thông báo
        notificationService.sendMessageNotification(
                userOther.getFcmToken(),
                response,
                conversation.getId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Gửi ảnh thành công", response));
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


//    @GetMapping("/{message_id}/image")
//    @Operation(summary = "Lấy ảnh message", description = "Trả về ảnh binary từ MinIO")
//    public ResponseEntity<byte[]> getImage(
//            @Parameter(description = "ID message")
//            @PathVariable("message_id")
//            UUID messageId
//
//    ) throws Exception {
//        InputStream inputStream = messageService.findImageMessageById(messageId);
//
//        return ResponseEntity
//                .ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .body(inputStream.readAllBytes());
//    }
}
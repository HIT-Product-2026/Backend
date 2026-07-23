package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.MessagePageResponse;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.ForbiddenException;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ConversationService;
import com.example.lockly.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(ApiPath.API_NOW + "/conversations")
@Tag(name = "Conversation Controller", description = "API quản lý hội thoại")
public class ConversationController {

    private final ConversationService conversationService;
    private final AuthService authService;
    private final MessageService messageService;

    @GetMapping("/{conversation_id}")
    @Operation(summary = "Lấy conversation theo id")
    public ResponseEntity<ApiResponse<ConversationResponseDto>> getConversationById(
            @PathVariable("conversation_id") UUID conversationId
    ) {
        User user = authService.getCurrentUser();

        ConversationResponseDto response = conversationService.findById(conversationId);
        if (response.user1().id() != user.getId()
        || response.user2().id() != user.getId())
            throw new ForbiddenException("User này không có quyền với conversation này");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", response));
    }


    @PostMapping
    @Operation(summary = "Tạo conversation mới")
    public ResponseEntity<ApiResponse<ConversationResponseDto>> createConversation(
            @Parameter(description = "Id của người muốn nhắn tin")
            @RequestParam(name = "userId") UUID userId
    ) {
        User user = authService.getCurrentUser();

        CreateConversationRequestDto request = new CreateConversationRequestDto(
                user.getId(),
                userId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Tạo conversation thành công",
                        conversationService.createConversation(request)
                ));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Lấy danh sách hội thoại của user")
    public ResponseEntity<ApiResponse<ListResponse<ConversationResponseDto>>> getConversations(
            @Parameter(description = "Số lượng conversation muốn nhận (mặc định là 10)")
            @RequestParam(name = "pageSize", required = false) Integer pageSize,

            @RequestParam(name = "cursor", required = false) String cursor
    ) {

        List<ConversationResponseDto> result = conversationService.findAll();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<ListResponse<MessageResponseDto>>> findMessagesByConversationId(
            @RequestParam(name = "conversation_id") UUID conversationId,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "pageSize", required = false) Integer pageSize
    ) {
        MessagePageResponse result =
                messageService.findMessagesByConversationId(
                        conversationId,
                        cursor,
                        pageSize
                );

        log.debug("Lấy danh sách message theo conversation id thành công");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(
                        result.messages(),
                        result.nextCursor()
                )));
    }
}
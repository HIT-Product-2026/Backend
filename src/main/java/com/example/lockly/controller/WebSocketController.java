package com.example.lockly.controller;

import com.example.lockly.domain.dto.request.SendImageMessageSocketRequestDto;
import com.example.lockly.domain.dto.request.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import com.example.lockly.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Controller
@RequiredArgsConstructor
@Tag(name = "WebSockets", description = "Quản lý chức năng realtime")
public class WebSocketController {

    private final WebSocketService webSocketService;
    private final MessageService messageService;
    private final RedisService redisService;
    private final AuthService authService;
    private final LocationService locationService;

    @MessageMapping("/chat.sendText")
    public void sendTextMessage(
            @Parameter(description = "Gửi tin nhắn dạng văn bản")
            @Valid SendTextMessageRequestDto request
    ) {
        // Lưu vào db
        MessageResponseDto response = messageService.sendTextMessage(request);

        // Response cho client
        webSocketService.sendTextMessage(request.conversationId(), response);
    }


    @MessageMapping("/chat.sendImage")
    @Operation(summary = "Gửi tin nhắn dạng ảnh", description = "Upload ảnh để gửi tin nhắn")
    public void sendImageMessage(
            @Parameter(description = "Gửi tin nhắn dạng ảnh")
            SendImageMessageSocketRequestDto request

    ) {
        //Request để lưu vào db trước rồi gửi imageUrl qua đây

        // Trả kết quả qua socket
        webSocketService.sendImageMessage(
                request.conversationId(),
                request.imageUrl());

    }

    @MessageMapping("/share.location")
    @Operation(
            summary = "Chia sẻ vị trí của người dùng",
            description = "Dùng để cập nhật vị trí user và vị trí được chia sẻ lên /topic/location/ + userId"
    )
    public void shareLocation(
            @Parameter(description = "Kinh độ")
            Double longitude,

            @Parameter(description = "Vĩ độ")
            Double latitude
    ){
        User user = authService.getCurrentUser();

        boolean isDropRequest = locationService.isDropRequest(longitude, latitude);

        // Kiểm tra xem request có được chấp nhận không (để giảm tần suất request)
        if (isDropRequest)
            return;

        // Lưu vào redis
        redisService.saveUserLocation(user.getId(), latitude, longitude);
        LocationUserResponseDto response = redisService.getUserLocation(user.getId());

        if (user.getMode() == UserMode.PRIVATE)
            return;

        // Chuyển lên topic cá nhân
        webSocketService.shareLocationToFriend(user.getId(), response);
    }
}

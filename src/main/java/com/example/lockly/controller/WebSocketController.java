package com.example.lockly.controller;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.domain.dto.request.create.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.create.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;
import java.util.UUID;

@Validated
@Controller
@RequiredArgsConstructor
@Tag(name = "WebSockets", description = "Quản lý chức năng realtime")
public class WebSocketController {

    private final WebSocketService webSocketService;
    private final MessageService messageService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final UserService userService;

    @MessageMapping("/chat.sendText")
    public void sendTextMessage(
            Authentication authentication,
            @Valid SendTextMessageRequestDto request
    ) {
        CustomUserDetails details =
                (CustomUserDetails) authentication.getPrincipal();

        User user = details.getUser();
        // Lưu vào db
        MessageResponseDto response = messageService.sendTextMessage(request, user);

        // Response cho client
        webSocketService.sendTextMessage(
                request.conversationId(),
                response
        );
    }


    @MessageMapping("/chat.sendImage")
    public void sendImageMessage(
            Authentication authentication,
            @Valid SendImageMessageRequestDto request
    ) {
        CustomUserDetails details =
                (CustomUserDetails) authentication.getPrincipal();

        User user = details.getUser();

        //Request để lưu vào db trước rồi gửi imageUrl qua đây
        MessageResponseDto response = messageService.sendImageMessage(request, user);

        // Trả kết quả qua socket
        webSocketService.sendImageMessage(
                request.conversationId(),
                response
        );
    }

    @MessageMapping("/share.location")
    public void sendLocationToUserFriends(
            Principal principal,
            Double longitude,
            Double latitude
    ){
        if (principal == null || principal.getName() == null) return;

        if (longitude == null || latitude == null) return;

        // Kiểm tra xem request có được chấp nhận không (để giảm tần suất request)
        if (locationService.isDropRequest(longitude, latitude)) return;

        UUID userId = UUID.fromString(principal.getName());

        UserCacheDto user = redisService.getUser(userId);

        if (user == null) {
            user = UserCacheDto.from(userRepository.findById(userId).orElseThrow());
            redisService.saveUser(user);
        }

        // Lưu vào redis
        redisService.saveUserLocation(user.id(), latitude, longitude);

        if (user.mode() == UserMode.PRIVATE)
            return;

        LocationUserResponseDto response = redisService.getUserLocation(user.id());

        // Chuyển lên topic cá nhân
        webSocketService.shareLocationToFriend(user.id(), response);
    }

    @MessageMapping("/online")
    public void IsUserOnline(Principal principal){

        UUID userId = UUID.fromString(principal.getName());

        UserCacheDto user = redisService.getUser(userId);

        if (user == null) {
            user = UserCacheDto.from(userRepository.findById(userId).orElseThrow());
            redisService.saveUser(user);
        }

        boolean isOnline = userService.isUserOnlineByUserId(user.id());

        // Redis lưu trạng thái online
        redisService.saveUserOnline(user.id(), isOnline);

        // Chuyển lên topic cá nhân
        webSocketService.shareOnlineToFriend(user.id(), isOnline);
    }
}

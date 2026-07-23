package com.example.lockly.controller;

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
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

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
    private final ConversationService conversationService;

    @MessageMapping("/chat.sendText")
    public void sendTextMessage(
            SimpMessageHeaderAccessor headerAccessor,
            @Valid SendTextMessageRequestDto request
    ) {

        User user = getCurrentUser(headerAccessor);

        // Lưu vào db
        MessageResponseDto response = messageService.sendTextMessage(request, user);

        // Response cho client thông qua topic chat
        webSocketService.sendTextMessage(
                request.conversationId(),
                response
        );

        // Lấy user còn lại trong coversation
        // Vì người đó mới là người cần nhận thông báo về tin nhắn)
        User userOther = conversationService.getOtherUser(
                request.conversationId(),
                user.getId()
        );

        // Gửi thông báo sang màn của người nhận tin nhắn
        webSocketService.pubMessageToConversations(
            userOther.getUsername(),
            response
        );
    }


    @MessageMapping("/chat.sendImage")
    public void sendImageMessage(
            SimpMessageHeaderAccessor headerAccessor,
            @Valid SendImageMessageRequestDto request
    ) {

        User user = getCurrentUser(headerAccessor);

        //Request để lưu vào db trước rồi gửi imageUrl qua đây
        MessageResponseDto response = messageService.sendImageMessage(request, user);

        // Trả kết quả qua socket
        webSocketService.sendImageMessage(
                request.conversationId(),
                response
        );

        // Lấy user còn lại trong coversation
        // Vì người đó mới là người cần nhận thông báo về tin nhắn)
        User userOther = conversationService.getOtherUser(
                request.conversationId(),
                user.getId()
        );

        // Gửi thông báo sang màn của người nhận tin nhắn
        webSocketService.pubMessageToConversations(
                userOther.getUsername(),
                response
        );
    }

    @MessageMapping("/share.location")
    public void sendLocationToUserFriends(
            SimpMessageHeaderAccessor headerAccessor,
            Double longitude,
            Double latitude
    ){

        User user = getCurrentUser(headerAccessor);

        if (longitude == null || latitude == null) return;

        // Kiểm tra xem request có được chấp nhận không (để giảm tần suất request)
        if (locationService.isDropRequest(longitude, latitude)) return;

        UserCacheDto userDto = redisService.getUser(user.getId());

        if (userDto == null) {
            userDto = UserCacheDto.from(userRepository.findById(user.getId()).orElseThrow());
            redisService.saveUser(userDto);
        }

        // Lưu vào redis
        redisService.saveUserLocation(userDto.id(), latitude, longitude);

        if (userDto.mode() == UserMode.PRIVATE)
            return;

        LocationUserResponseDto response = redisService.getUserLocation(userDto.id());

        // Chuyển lên topic cá nhân
        webSocketService.shareLocationToFriend(userDto.id(), response);
    }

    @MessageMapping("/online")
    public void IsUserOnline(SimpMessageHeaderAccessor headerAccessor){

        User user = getCurrentUser(headerAccessor);

        UserCacheDto userDto = redisService.getUser(user.getId());

        if (userDto == null) {
            userDto = UserCacheDto.from(userRepository.findById(user.getId()).orElseThrow());
            redisService.saveUser(userDto);
        }

        boolean isOnline = userService.isUserOnlineByUserId(userDto.id());

        // Redis lưu trạng thái online
        redisService.saveUserOnline(userDto.id(), isOnline);

        // Chuyển lên topic cá nhân
        webSocketService.shareOnlineToFriend(userDto.id(), isOnline);
    }

    private User getCurrentUser(SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser();

        if (authentication == null) {
            throw new RuntimeException("Authentication is null");
        }

        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }
}

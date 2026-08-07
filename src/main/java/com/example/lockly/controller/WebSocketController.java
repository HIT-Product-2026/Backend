package com.example.lockly.controller;

import com.example.lockly.domain.dto.request.ShareLocationRequest;
import com.example.lockly.domain.dto.request.create.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.create.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.dto.response.common.ConversationRealtimeResponseDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.mapper.conversation.ConversationRealtimeResponseMapper;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Validated
@Controller
@Slf4j
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
    private final ConversationRepository conversationRepository;
    private final NotificationService notificationService;
    private final ConversationRealtimeResponseMapper conversationRealtimeResponseMapper;

    @MessageMapping("/chat.sendText")
    public void sendTextMessage(
            SimpMessageHeaderAccessor headerAccessor,
            @Valid SendTextMessageRequestDto request
    ) {

        User user = getCurrentUser(headerAccessor);

        // Lưu vào db
        MessageResponseDto response = messageService.sendTextMessage(request, user);
        log.info("Lưu thành công");

        // Response cho client thông qua topic chat
        webSocketService.sendTextMessage(
                request.conversationId(),
                response
        );
        log.debug("Gửi thành công");

        // Lấy user còn lại trong coversation
        // Vì người đó mới là người cần nhận thông báo về tin nhắn)
        User userOther = conversationService.getOtherUser(
                request.conversationId(),
                user.getId()
        );

        Conversation conversation = conversationRepository
                .findByIdWithUsers(response.conversationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation",
                        "conversation id",
                        response.conversationId()
                ));

        ConversationRealtimeResponseDto dto =
                conversationRealtimeResponseMapper.from(conversation);

        // Gửi response sang màn của người nhận tin nhắn
        webSocketService.pubMessageToConversations(
            userOther.getUsername(),
            dto
        );
        log.info("Gửi response thành công");

        // Gửi thông báo
        notificationService.sendMessageNotification(
                userOther.getFcmToken(),
                response,
                conversation.getId()
        );
        log.info("Gửi thông báo thành công");
    }

    @MessageMapping("/chat.sendImage")
    public void sendImageMessage(
            SimpMessageHeaderAccessor headerAccessor,
            @Valid SendImageMessageRequestDto request
    ) {

        log.info("Url image: " + request.imageUrl());
        log.info("conversation id: " + request.conversationId());
        log.info("Is read: " + request.isRead());

        User user = getCurrentUser(headerAccessor);

        // Lưu vào DB
        MessageResponseDto response = messageService.sendImageMessage(request, user);

        // Gửi tin nhắn vào màn hình chat
        webSocketService.sendImageMessage(
                request.conversationId(),
                response
        );
        log.info("Gửi thành công");

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
                conversationRealtimeResponseMapper.from(conversation);

        // Gửi cập nhật danh sách conversation cho người nhận
        webSocketService.pubMessageToConversations(
                userOther.getUsername(),
                dto
        );
        log.debug("Gửi response thành công");

        // Gửi thông báo
        notificationService.sendMessageNotification(
                userOther.getFcmToken(),
                response,
                conversation.getId()
        );
        log.debug("Gửi thông báo thành công");
    }

    @MessageMapping("/share.location")
    public void sendLocationToUserFriends(
            SimpMessageHeaderAccessor headerAccessor,
            ShareLocationRequest request
    ){

        log.debug("Đã nhận request location");

        User user = getCurrentUser(headerAccessor);

        Double longitude = request.longitude();
        Double latitude = request.latitude();

        // Kiểm tra xem request có được chấp nhận không (để giảm tần suất request)
        if (locationService.isDropRequest(longitude, latitude, user)) return;

        log.debug("Request hợp lệ");

        // Kiểm tra tài khoản có bị đăng nhập từ nơi khác không
//        boolean isUpdateFromUknownLocation = locationService.isUnknownLocation(longitude, latitude, user);
//        if (isUpdateFromUknownLocation) {
//            // logout
//            authService.forceLogout(user.getId());
//
//            // Tự động thay password, buộc người dùng phải đổi lại password
//            authService.forceResetPassword(user);
//        }

        log.debug("longitude: " + longitude);
        log.debug("latitude" + latitude);

        UserCacheDto userDto = redisService.getUser(user.getId());

        if (userDto == null) {
            userDto = UserCacheDto.from(user);
            redisService.saveUser(userDto);
        }

        // Lưu vào redis
        redisService.saveUserLocation(userDto.id(), latitude, longitude);
        log.debug("Lưu thành công location");

        LocationUserResponseDto response = redisService.getUserLocation(user.getId());

        log.debug("Bắt đầu chia sẻ vị trí");

        // Private thì không chia sẻ vị trí
        if (user.getMode() == UserMode.PRIVATE){
            response = LocationUserResponseDto.from(
                    response.userId(),
                    null,
                    null,
                    response.lastActiveAt()
            );
        }

        // Chuyển lên topic cá nhân
        webSocketService.shareLocationToFriend(userDto.toEntity(), response);

        log.debug("Chia sẻ thành công");
    }

    @MessageMapping("/online")
    public void IsUserOnline(SimpMessageHeaderAccessor headerAccessor){

        User user = getCurrentUser(headerAccessor);

        UserCacheDto userDto = redisService.getUser(user.getId());

        if (userDto == null) {
            userDto = UserCacheDto.from(userRepository.findById(user.getId()).orElseThrow());
            redisService.saveUser(userDto);
        }

        boolean isOnline = userService.isUserOnlineByUserId(user);

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

        CustomUserDetails details =
                (CustomUserDetails) authentication.getPrincipal();

        User user;

        UserCacheDto dto = redisService.getUser(details.getId());

        if (dto == null){
            user = userRepository.findById(details.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User", "id", details.getId()));

            redisService.saveUser(UserCacheDto.from(user));
        } else {
            user = dto.toEntity();
        }

        return user;
    }
}

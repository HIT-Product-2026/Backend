package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.exception.nonRetryException.InvalidFcmRequestException;
import com.example.lockly.service.FcmService;
import com.example.lockly.service.LocationService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final LocationService locationService;

    private void validate(FcmNotificationRequestDto data) {

        if (data == null) {
            throw new InvalidFcmRequestException("FCM request is null");
        }

        if (data.senderId() == null) {
            throw new InvalidFcmRequestException("Sender ID is required");
        }

        if (data.postId() == null) {
            throw new InvalidFcmRequestException("Post ID is required");
        }

        if (data.type() == null) {
            throw new InvalidFcmRequestException("FCM message type is required");
        }

        List<String> tokens = data.fcmToken();

        if (tokens == null) {
            throw new InvalidFcmRequestException("FCM token list is required");
        }

        if (tokens.size() > 500) {
            throw new InvalidFcmRequestException("FCM token list cannot exceed 500 items");
        }

        for (int i = 0; i < tokens.size(); i++) {

            if (tokens.get(i) == null) {
                throw new InvalidFcmRequestException(
                        "FCM token at index " + i + " is null"
                );
            }

            if (tokens.get(i).isBlank()) {
                throw new InvalidFcmRequestException(
                        "FCM token at index " + i + " is blank"
                );
            }
        }

        if (data.displayName() == null)
            throw new InvalidFcmRequestException("Display name cannot required");

        if (data.imageUrl() == null)
            throw new InvalidFcmRequestException("Image cannot required");

        if (data.latitude() == null || data.longitude() == null)
            throw new InvalidFcmRequestException("Location cannot required");
    }

    @Override
    public void sendToManySilent(FcmNotificationRequestDto data){
        List<String> fcmTokens = data.fcmToken();

        if (fcmTokens == null || fcmTokens.isEmpty()) {
            // Không có bạn bè thì không gửi
            return;
        }

        log.info("FCM Tokens: {}", fcmTokens);

        for (int i = 0; i < fcmTokens.size(); i++) {
            log.info("token[{}] = {}", i, fcmTokens.get(i));
        }

        validate(data);

        String avatarUrl = data.avatarUrl();
        String caption = data.caption();
        String provinceName = locationService.getProvinceFullName(data.latitude(), data.longitude());
        String wardName = locationService.getWardFullName(data.latitude(), data.longitude());


        if (avatarUrl == null) {
            avatarUrl = "";
        }
        if (caption == null) {
            caption = "";
        }

        if (provinceName == null){
            provinceName = "";
        }

        if (wardName == null){
            wardName = "";
        }


        try {
            MulticastMessage message =
                    MulticastMessage.builder()
                            .addAllTokens(fcmTokens)
                            .putData("sender_id", data.senderId().toString())
                            .putData("post_id", data.postId().toString())
                            .putData("type", data.type().name())
                            .putData("display_name", data.displayName())
                            .putData("avatar_url", avatarUrl)
                            .putData("image_url", data.imageUrl())
                            .putData("latitude", data.latitude().toString())
                            .putData("longitude", data.longitude().toString())
                            .putData("province_name", provinceName)
                            .putData("ward_name", wardName)
                            .putData("caption", caption)
                            .putData("type_post", data.typePost().toString())
                            .build();

            BatchResponse batchResponse =
                    FirebaseMessaging.getInstance()
                            .sendEachForMulticast(message);

            log.info(
                    "FCM success: {}, failed: {}",
                    batchResponse.getSuccessCount(),
                    batchResponse.getFailureCount()
            );

        } catch (Exception e){
            throw new RuntimeException("Failed to send FCM message", e);
        }
    }
}

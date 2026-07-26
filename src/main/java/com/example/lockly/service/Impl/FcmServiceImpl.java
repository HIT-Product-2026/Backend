package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.entity.main.enumEntity.FcmMessageType;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.FcmService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final AuthService authService;

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


        try {
            MulticastMessage message =
                    MulticastMessage.builder()
                            .addAllTokens(fcmTokens)
                            .putData("sender_id", data.senderId().toString())
                            .putData("post_id", data.postId().toString())
                            .putData("type", data.type().name())
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

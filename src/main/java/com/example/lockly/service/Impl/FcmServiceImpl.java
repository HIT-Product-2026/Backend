package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.FcmPostResponseDto;
import com.example.lockly.domain.entity.FcmMessageType;
import com.example.lockly.service.FcmService;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FcmServiceImpl implements FcmService {

    @Override
    public void sendToManySilent(List<String> fcmTokens, FcmPostResponseDto response){
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            // Không có bạn bè thì không gửi
            return;
        }
        try {
            MulticastMessage message =
                    MulticastMessage.builder()

                            .addAllTokens(fcmTokens)

                            .putData("sender_id", response.senderId())
                            .putData("post_id", response.postId())
                            .putData("type", response.type().name())

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


    @Override
    public FcmPostResponseDto createFcmPostResponse(String senderId, String postId){
        return new FcmPostResponseDto(
                senderId,
                postId,
                FcmMessageType.POST
        );
    }
}

package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Message;
import com.example.lockly.domain.entity.main.enumEntity.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MessageResponseDto {
    UUID id;
    UserResponseDto sender;
    //Nếu là ảnh thì content sẽ chứa api của ảnh
    String content;
    MessageType type;
    LocalDateTime createdAt;
    Boolean isRead;

    public static MessageResponseDto from(Message message){
        return new MessageResponseDto(
                message.getId(),
                UserResponseDto.from(message.getSender()),
                message.getContent(),
                message.getType(),
                message.getCreatedAt(),
                message.isRead()
        );
    }

    public static MessageResponseDto from(Message message, String imageUrl) {
        return new MessageResponseDto(
                message.getId(),
                UserResponseDto.from(message.getSender()),
                imageUrl,
                message.getType(),
                message.getCreatedAt(),
                message.isRead()
        );
    }
}

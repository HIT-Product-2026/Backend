package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.Message;
import com.example.lockly.domain.entity.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MessageResponseDto {

    UserResponseDto sender;
    //Nếu là ảnh thì content sẽ chứa api của ảnh
    String content;
    MessageType type;
    LocalDateTime createdAt;

    public static MessageResponseDto from(Message message){
        return new MessageResponseDto(
                UserResponseDto.from(message.getSender()),
                message.getContent(),
                message.getType(),
                message.getCreatedAt()
        );
    }
}

package com.example.lockly.mapper.message;

import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.Message;
import com.example.lockly.mapper.user.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResponseMapper {

    private final UserResponseMapper userResponseMapper;

    public MessageResponseDto from(Message message, String urlImage) {
        return new MessageResponseDto(
                message.getId(),
                userResponseMapper.from(message.getSender()),
                message.getConversation().getId(),
                urlImage,
                message.getContent(),
                message.getType(),
                message.getCreatedAt(),
                message.isRead()
        );
    }
}

package com.example.lockly.mapper.conversation;

import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.mapper.user.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationResponseMapper {

    private final UserResponseMapper userResponseMapper;

    public ConversationResponseDto from(Conversation conversation){
        return new ConversationResponseDto(
                conversation.getId(),
                userResponseMapper.from(conversation.getUser1()),
                userResponseMapper.from(conversation.getUser2()),
                conversation.getLastMessageContent(),
                conversation.getLastMessageTime()
        );
    }
}

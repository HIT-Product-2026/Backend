package com.example.lockly.mapper.conversation;

import com.example.lockly.domain.dto.response.common.ConversationSimpleResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.mapper.user.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationSimpleResponseMapper {

    private final UserResponseMapper userResponseMapper;

    public ConversationSimpleResponseDto from(Conversation conversation){
        if (conversation == null) {
            return null;
        }

        return new ConversationSimpleResponseDto(
                conversation.getId(),
                userResponseMapper.from(conversation.getUser1()),
                userResponseMapper.from(conversation.getUser2())
        );
    }
}

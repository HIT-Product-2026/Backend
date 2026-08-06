package com.example.lockly.mapper.conversation;

import com.example.lockly.domain.dto.response.common.ConversationRealtimeResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.mapper.user.UserSimpleResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationRealtimeResponseMapper {

    private final UserSimpleResponseMapper userSimpleResponseMapper;

    public ConversationRealtimeResponseDto from(Conversation conversation){
        return new ConversationRealtimeResponseDto(
                conversation.getId(),
                userSimpleResponseMapper.from(conversation.getUser1()),
                userSimpleResponseMapper.from(conversation.getUser2()),
                conversation.getLastMessageContent(),
                conversation.getLastMessageTime()
        );
    }
}

package com.example.lockly.mapper.emoji;

import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.entity.main.EmojiPost;
import com.example.lockly.mapper.user.UserSimpleResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmojiPostResponseMapper {

    private final UserSimpleResponseMapper userSimpleResponseMapper;

    public EmojiPostResponseDto from(EmojiPost emojiPost){
        return new EmojiPostResponseDto(
                emojiPost.getId(),
                emojiPost.getPost().getId(),
                userSimpleResponseMapper.from(emojiPost.getSender()),
                emojiPost.getEmoji(),
                emojiPost.getCreatedAt()
        );
    }
}

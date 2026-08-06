package com.example.lockly.mapper.friendship;

import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.mapper.user.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendshipsResponseMapper {

    private final UserResponseMapper userResponseMapper;

    public <T> FriendshipsResponseDto from(Friendship friendship){
        return new FriendshipsResponseDto(
                friendship.getId(),
                userResponseMapper.from(friendship.getRequester()),
                userResponseMapper.from(friendship.getReceiver()),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getUpdatedAt()
        );
    }
}

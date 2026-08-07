package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.UserBlock;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.DuplicateResourceException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.mapper.user.UserSimpleResponseMapper;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.FriendshipsRepository;
import com.example.lockly.repository.main.UserBlockRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final ConversationRepository conversationRepository;
    private final UserSimpleResponseMapper userSimpleResponseMapper;

    @Override
    @Transactional
    public void blockUser(User blockerCache, UUID blockedId) {
        User blocker = userRepository.findByIdAndDeletedAtIsNull(blockerCache.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockerCache.getId()));

        User blocked = userRepository.findByIdAndDeletedAtIsNull(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockedId));

        if (blocker.getId().equals(blocked.getId())) {
            throw new BadRequestException("Không thể tự chặn chính mình");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new DuplicateResourceException("User has already been blocked");
        }

        friendshipsRepository
                .findFriendshipBetweenUsers(blocker, blocked)
                .ifPresent(friendshipsRepository::delete);

        Conversation conversation = conversationRepository
                .findByUsers(blocker, blocked)
                .orElse(null);

        if (conversation != null) {
            conversationRepository.delete(conversation);
        }

        UserBlock userBlock = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        userBlockRepository.save(userBlock);
    }

    @Override
    @Transactional
    public void unblockUser(User blockerCache, UUID blockedId) {
        User blocker = userRepository.findByIdAndDeletedAtIsNull(blockerCache.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockerCache.getId()));

        User blocked = userRepository.findByIdAndDeletedAtIsNull(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockedId));

        UserBlock userBlock = userBlockRepository
                .findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new ResourceNotFoundException("UserBlock", "blockedId", blockedId));

        userBlockRepository.delete(userBlock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSimpleResponseDto> getBlockedUsers(User blockerCache) {
        User blocker = userRepository.findByIdAndDeletedAtIsNull(blockerCache.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockerCache.getId()));

        return userBlockRepository
                .findByBlockerWithBlocked(blocker)
                .stream()
                .map(UserBlock::getBlocked)
                .map(userSimpleResponseMapper::from)
                .toList();
    }
}
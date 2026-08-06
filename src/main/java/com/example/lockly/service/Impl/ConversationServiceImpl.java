package com.example.lockly.service.Impl;

import com.example.lockly.common.util.CursorUtil;
import com.example.lockly.domain.dto.request.Cursor;
import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.DuplicateResourceException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.mapper.conversation.ConversationResponseMapper;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ConversationResponseMapper conversationResponseMapper;

//    private final int pageSize = 10;

    @Override
    public List<ConversationResponseDto> findAll(){
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        return conversationRepository
                .findByUser(user)
                .stream()
                .map(conversationResponseMapper::from)
                .toList();
    }

    @Override
    public String getOtherUsername(UUID conversationId, UUID currentUserId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow();

        if (conversation.getUser1().getId().equals(currentUserId)) {
            return conversation.getUser2().getUsername();
        }

        return conversation.getUser1().getUsername();
    }

    @Override
    public ConversationResponseDto createConversation(CreateConversationRequestDto request){
        User user1 = userRepository
                .findById(request.userId1())
                .orElseThrow(() -> new BadRequestException("User id", request.userId1()));

        User user2 = userRepository
                .findById(request.userId2())
                .orElseThrow(() -> new BadRequestException("User id", request.userId2()));



        if (user1.getId().compareTo(user2.getId()) > 0){
            User userTmp = user1;
            user1 = user2;
            user2 = userTmp;
        }

        if (conversationRepository.existsByUser1AndUser2(user1, user2))
            throw new DuplicateResourceException("Conversation này đã tồn tại");

        Conversation conversation = Conversation.builder()
                .user1(user1)
                .user2(user2)
                .build();

        return conversationResponseMapper.from(conversationRepository.save(conversation));
    }

    @Override
    public ConversationResponseDto findById(UUID id){
        Conversation conversation = conversationRepository
                .findById(id)
                .orElseThrow(() -> new BadRequestException("Conversation id", id));

        return conversationResponseMapper.from(conversation);
    }


    public User getOtherUser(UUID conversationId, UUID userId) {

        Conversation conversation =
                conversationRepository.findByIdWithUsersAndLastMessage(conversationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conversation",
                                        "id",
                                        conversationId
                                ));

        if (conversation.getUser1().getId().equals(userId)) {
            return conversation.getUser2();
        }

        return conversation.getUser1();
    }
}

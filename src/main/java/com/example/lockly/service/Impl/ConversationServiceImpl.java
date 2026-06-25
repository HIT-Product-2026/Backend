package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.entity.Conversation;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.repository.ConversationRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ConversationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationServiceImpl implements ConversationService {

    ConversationRepository conversationRepository;
    UserRepository userRepository;
    AuthService authService;

    @Override
    public List<ConversationResponseDto> findAll(){
        User user = authService.getCurrentUser();

        return conversationRepository
                .findByUser(user)
                .stream()
                .map(ConversationResponseDto::from)
                .toList();
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

        return ConversationResponseDto.from(conversationRepository.save(conversation));
    }

    @Override
    public ConversationResponseDto findById(String id){
        Conversation conversation = conversationRepository
                .findById(id)
                .orElseThrow(() -> new BadRequestException("Conversation id", id));

        return ConversationResponseDto.from(conversation);
    }

}

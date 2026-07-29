package com.example.lockly.service.Impl;

import com.example.lockly.common.util.CursorUtil;
import com.example.lockly.common.util.FileUtil;
import com.example.lockly.domain.dto.request.Cursor;
import com.example.lockly.domain.dto.request.create.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.create.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.MessagePageResponse;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.Message;
import com.example.lockly.domain.entity.main.enumEntity.MessageType;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.ForbiddenException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.MessageRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.MessageService;
import com.example.lockly.service.MinIOService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AuthService authService;
    private final MinIOService minIOService;

    private final String prefix = "message";
    private final Integer pageSize = 10;

    private void validateSender(Conversation conversation, User sender){
        if(!conversation.getUser1().getId().equals(sender.getId())
                && !conversation.getUser2().getId().equals(sender.getId())
        ){
            throw new ForbiddenException("You are not allowed to send message to this conversation_id: ",
                    conversation.getId()
            );

        }

    }

    @Override
    @Transactional
    public MessageResponseDto sendTextMessage(SendTextMessageRequestDto request, User user){

        Conversation conversation = conversationRepository
                .findById(request.conversationId())
                .orElseThrow(() -> new BadRequestException("Conversation id", request.conversationId()));

        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be empty", request.content());
        }

        //Kiểm tra người gửi có thuộc về đoạn chat không
        validateSender(conversation, user);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(user)
                .objectName(null)
                .type(MessageType.TEXT)
                .content(request.content().trim())
                .isRead(request.isRead())
                .build();

        messageRepository.save(message);

        conversation.setLastMessage(message);
        conversation.setLastMessageTime(message.getCreatedAt());
        conversation.setLastMessageContent(message.getContent());

        conversationRepository.save(conversation);

        return MessageResponseDto.from(message, null);
    }

    @Override
    @Transactional
    public MessageResponseDto sendImageMessage(SendImageMessageRequestDto request, User user){

        Conversation conversation = conversationRepository
                .findById(request.conversationId())
                .orElseThrow(() -> new BadRequestException("Conversation id", request.conversationId()));

        if (request.imageUrl() == null || request.imageUrl().isEmpty()) {
            throw new BadRequestException("Image url is empty");
        }

        //Kiểm tra người gửi có thuộc về đoạn chat không
        validateSender(conversation, user);

        UUID messageId = UuidCreator.getTimeOrderedEpoch();

        String objectName = FileUtil.extractObjectName(request.imageUrl());

        Message message = Message.builder()
                .id(messageId)
                .conversation(conversation)
                .sender(user)
                .type(MessageType.IMAGE)
                .content(null)
                .objectName(objectName)
                .isRead(request.isRead())
                .build();

        messageRepository.save(message);

        conversation.setLastMessage(message);
        conversation.setLastMessageTime(message.getCreatedAt());
        conversation.setLastMessageContent("Đã gửi 1 ảnh");

        conversationRepository.save(conversation);

        return MessageResponseDto.from(
                messageRepository.save(message),
                request.imageUrl()
        );

    }


    @Override
    public MessagePageResponse findMessagesByConversationId(
            UUID conversationId,
            String cursor,
            Integer pageSize
    ){
        log.debug("Bắt đầu lấy message theo conversation id");

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new BadRequestException("conversation id", conversationId));

        User user = authService.getCurrentUser();

        validateSender(conversation, user);

        Slice<Message> messageList;

        log.debug("pageSize: " + pageSize);
        log.debug("cursor: " + cursor);

        if (pageSize == null)
            pageSize = this.pageSize;

        if (cursor == null || cursor.isBlank()){
            log.debug("Lấy trang message đầu trong phân trang");
            messageList = messageRepository.findByConversationFirstPage(
                    conversationId,
                    PageRequest.of(0, pageSize)
            );
        } else {
            log.debug("Lấy trang message tiếp theo trong phân trang");

            Cursor cursorDecode = CursorUtil.decode(cursor);

            messageList = messageRepository.findByConversationNextPage(
                    conversationId,
                    cursorDecode.createdAt(),
                    cursorDecode.id(),
                    PageRequest.of(0, pageSize)
            );
        }

        List<Message> listMessage = messageList.getContent();

        String nextCursor = null;
        if (!listMessage.isEmpty()) {
            nextCursor = CursorUtil.encode(
                    Cursor.from(listMessage.get(listMessage.size() - 1))
            );
        }

        log.debug("nextCursor: " + nextCursor);

        return this.buildMessageResponseDtos(
                listMessage,
                nextCursor
        );
    }

    @Override
    public MessageResponseDto findMessageById(UUID id){
        Message message = messageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        MessageResponseDto dto;

        if (message.getType() == MessageType.IMAGE){
            dto = MessageResponseDto.from(
                    message,
                    minIOService.generatePresignedUrl(message.getObjectName())
            );
        } else {
            dto = MessageResponseDto.from(message, null);
        }

        return dto;
    }

    @Override
    public InputStream findImageMessageById(UUID id){
        Message message = messageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        if (message.getType() != MessageType.IMAGE)
            throw new BadRequestException("Id này không phải là của image message");

        return minIOService.getFile(message.getContent());
    }

    @Override
    public MessagePageResponse buildMessageResponseDtos(List<Message> messages, String nextCursor){
        List<MessageResponseDto> messageDtos = messages.stream()
                .map(message -> MessageResponseDto.from(
                        message,
                        minIOService.generatePresignedUrl(message.getObjectName()))
                ).toList();


        return MessagePageResponse.from(
                messageDtos,
                nextCursor
        );
    }
}

package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.MessageResponseDto;
import com.example.lockly.domain.entity.Conversation;
import com.example.lockly.domain.entity.Message;
import com.example.lockly.domain.entity.MessageType;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ForbiddenException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.ConversationRepository;
import com.example.lockly.repository.MessageRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.MessageService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService {

    ConversationRepository conversationRepository;
    UserRepository userRepository;
    MessageRepository messageRepository;
    AuthService authService;
    MinIOService minIOService;
    String prefix = "message";

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
    public MessageResponseDto sendTextMessage(SendTextMessageRequestDto request){
        User user = authService.getCurrentUser();

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
                .type(MessageType.TEXT)
                .content(request.content().trim())
                .build();

        return MessageResponseDto.from(message);
    }

    @Override
    @Transactional
    public MessageResponseDto sendImageMessage(SendImageMessageRequestDto request) throws Exception{
        User user = authService.getCurrentUser();

        Conversation conversation = conversationRepository
                .findById(request.conversationId())
                .orElseThrow(() -> new BadRequestException("Conversation id", request.conversationId()));

        if (request.file() == null || request.file().isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        //Kiểm tra người gửi có thuộc về đoạn chat không
        validateSender(conversation, user);

        String messageId = UUID.randomUUID().toString();

        String objectName = FileUtil.getObjectNameFile(prefix, messageId, request.file());

        try {
            // Save file
            minIOService.saveFile(request.file(), objectName);

            Message message = Message.builder()
                    .id(messageId)
                    .conversation(conversation)
                    .sender(user)
                    .type(MessageType.IMAGE)
                    .content(objectName)
                    .build();

            return MessageResponseDto.from(messageRepository.save(message));
        } catch (Exception e) {
            minIOService.deleteFile(objectName);

            throw e;
        }
    }


    @Override
    public List<MessageResponseDto> findMessagesByConversationId(String conversationId){
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new BadRequestException("conversation id", conversationId));

        User user = authService.getCurrentUser();

        validateSender(conversation, user);

        List<Message> messageList = messageRepository.findByConversationId(conversation.getId());

        // content lưu trong message là đường dẫn trong minIO, nhưng trả về thì phải trả về url api để fe gọi
        return messageList.stream()
                .map(message -> {

                    MessageResponseDto dto =
                            MessageResponseDto.from(message);

                    if(message.getType() == MessageType.IMAGE){

                        dto.setContent(
                                FileUtil.getImageUrlApi(
                                        prefix,
                                        message.getId()
                                )
                        );
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    public MessageResponseDto findMessageById(String id){
        Message message = messageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        MessageResponseDto dto = MessageResponseDto.from(message);
        if(message.getType() == MessageType.IMAGE){
            dto.setContent(FileUtil.getImageUrlApi(prefix, message.getId()));
        }

        return dto;
    }

    @Override
    public InputStream findImageMessageById(String id) throws Exception{
        Message message = messageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        if (message.getType() != MessageType.IMAGE)
            throw new BadRequestException("Id này không phải là của image message");

        return minIOService.getFile(message.getContent());
    }
}

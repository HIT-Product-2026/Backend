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
import com.example.lockly.repository.ConversationRepository;
import com.example.lockly.repository.MessageRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.ConversationService;
import com.example.lockly.service.MessageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl implements MessageService {

    ConversationService conversationService;
    ConversationRepository conversationRepository;
    UserRepository userRepository;
    MessageRepository messageRepository;
    MinioClient minioClient;
    MinioProperties props;
    String prefix = "messages";

    @Override
    public MessageResponseDto sendTextMessage(SendTextMessageRequestDto request){
        Conversation conversation = conversationRepository
                .findById(request.conversationId())
                .orElseThrow(() -> new BadRequestException("Conversation id", request.conversationId()));

        User sender = userRepository
                .findById(request.senderId())
                .orElseThrow(() -> new BadRequestException("User id", request.senderId()));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .type(MessageType.TEXT)
                .content(request.content())
                .build();

        return MessageResponseDto.from(messageRepository.save(message));
    }

    @Override
    public MessageResponseDto sendImageMessage(SendImageMessageRequestDto request) throws Exception{
        Conversation conversation = conversationRepository
                .findById(request.conversationId())
                .orElseThrow(() -> new BadRequestException("Conversation id", request.conversationId()));

        User sender = userRepository
                .findById(request.senderId())
                .orElseThrow(() -> new BadRequestException("User id", request.senderId()));

        String messageId = UUID.randomUUID().toString();

        String objectName = FileUtil.getObjectNameFile(prefix, messageId, request.file());

        // Save file
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(objectName)
                        .stream(
                                request.file().getInputStream(),
                                request.file().getSize(),
                                -1
                        )
                        .contentType(request.file().getContentType())
                        .build()
        );

        String content = props.getBucketName() + "/" + objectName;

        Message message = Message.builder()
                .id(messageId)
                .conversation(conversation)
                .sender(sender)
                .type(MessageType.IMAGE)
                .content(content)
                .build();

        return MessageResponseDto.from(messageRepository.save(message));
    }


    @Override
    public List<MessageResponseDto> getMessages(String conversationId){
        // content lưu trong message là đường dẫn trong minIO, nhưng trả về thì phải trả về url api để fe gọi
    }

    @Override
    public MessageResponseDto getMessageById(String id){

    }
}

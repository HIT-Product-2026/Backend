package com.example.lockly.service.Impl;

import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.service.AIService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.QdrantService;
import com.example.lockly.service.RequestAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final RequestAIService requestAIService;
    private final MinIOService minIOService;
    private final QdrantService qdrantService;

    @Override
    public Boolean detectNsfw(String objectName){

        MultipartFile file = minIOService.getMultipartFile(objectName);

        if (file == null){
            throw new ResourceNotFoundException("File", "objectName", objectName);
        }

        Boolean isNsfw = requestAIService.detectNsfw(file);

        return isNsfw;
    }

    @Override
    public Boolean registerFace(UUID userId, String objectName){
        MultipartFile file = minIOService.getMultipartFile(objectName);

        List<List<Float>> vectorEmbeddings = requestAIService.detectFaces(file);

        if (vectorEmbeddings.size() != 1){
            log.debug("Ảnh chỉ được phép có 1 khuôn mặt trong ảnh");
            return false;
        }

        qdrantService.save(
                userId,
                vectorEmbeddings.get(0)
        );

        return true;
    }

    @Override
    public List<UUID> detectFaces(String objectName){
        List<UUID> listId = new ArrayList<>();

        // Lấy ảnh
        MultipartFile file = minIOService.getMultipartFile(objectName);

        // Detect toàn bộ khuôn mặt trong ảnh
        List<List<Float>> vectorEmbeddings = requestAIService.detectFaces(file);

        if (vectorEmbeddings.isEmpty()){
            log.debug("Không phát hiện được khuôn mặt nào trong ảnh hoặc khuôn mặt phát hiện được chưa được đăng ký");
            return new ArrayList<>();
        }

        // Search từng khuôn mặt
        for (List<Float> embedding : vectorEmbeddings){
             UUID id = qdrantService.searchVectorEmbedding(embedding);
             if (id != null){
                 listId.add(id);
             }
        }

        return listId;
    }

    @Override
    public Boolean checkFace(UUID userId){
        List<Float> embedding = qdrantService.getEmbedding(userId);

        if (embedding != null)
            return true;

        return false;
    }
}
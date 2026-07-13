package com.example.lockly.service;

import java.util.List;
import java.util.UUID;

public interface QdrantService {
    Boolean save(UUID userId, List<Float> embedding);

    List<Float> getEmbedding(UUID userId);

    void delete(UUID userId);

    UUID searchVectorEmbedding(List<Float> embedding);
}

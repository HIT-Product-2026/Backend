package com.example.lockly.service;

import java.util.List;
import java.util.UUID;

public interface QdrantService {
    public void save(UUID userId, List<Float> embedding);

    public List<Float> getEmbedding(UUID userId);

    public void delete(UUID userId);

    public UUID searchVectorEmbedding(List<Float> embedding);
}

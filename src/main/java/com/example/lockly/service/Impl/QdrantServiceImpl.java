package com.example.lockly.service.Impl;

import com.example.lockly.service.QdrantService;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QdrantServiceImpl implements QdrantService {

    private final QdrantClient client;

    // Khởi tạo cấu trúc vector embedding 512 chiều
    @PostConstruct
    public void createCollection() throws Exception {
        client.createCollectionAsync(
                "users",
                Collections.VectorParams.newBuilder()
                        .setSize(512)
                        .setDistance(Collections.Distance.Cosine)
                        .build()
        ).get();
    }

    public void save(UUID userId, List<Float> embedding) throws Exception {

        Points.PointStruct point =
                Points.PointStruct.newBuilder()
                        .setId(PointIdFactory.id(userId.toString()))
                        .setVectors(
                                VectorsFactory.vectors(embedding)
                        )
                        .build();

        client.upsertAsync(
                "users",
                List.of(point)
        ).get();

    }
}

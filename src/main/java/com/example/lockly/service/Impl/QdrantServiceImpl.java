package com.example.lockly.service.Impl;

import com.example.lockly.exception.QdrantException;
import com.example.lockly.service.QdrantService;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QdrantServiceImpl implements QdrantService {

    private final QdrantClient client;
    private final float threshold = 0.7f;

    private static final String COLLECTION_NAME = "users";

    // Khởi tạo cấu trúc vector embedding 512 chiều
    // Giống table trong sql
    @PostConstruct
    public void createCollection() {
        // Nếu tồn tại rồi thì không khởi tạo nữa
        try {
            boolean exists =
                    client.collectionExistsAsync(COLLECTION_NAME)
                            .get();

            if (!exists) {
                client.createCollectionAsync(
                        COLLECTION_NAME,
                        Collections.VectorParams.newBuilder()
                                .setSize(512)
                                .setDistance(Collections.Distance.Cosine)
                                .build()
                ).get();
            }

        } catch (Exception e) {
            throw new QdrantException(
                    "Cannot initialize Qdrant collection",
                    e
            );
        }
    }

    @Override
    public Boolean save(UUID userId, List<Float> embedding) {
        try {
            Points.PointStruct point =
                    Points.PointStruct.newBuilder()
                            .setId(PointIdFactory.id(userId))
                            .setVectors(
                                    VectorsFactory.vectors(embedding)
                            )
                            .build();

            client.upsertAsync(
                    COLLECTION_NAME,
                    List.of(point)
            ).get();

            log.info("Register userId = {}", userId);

            return true;

        } catch (Exception e) {
            throw new QdrantException(
                    "Cannot save embedding for user: " + userId,
                    e
            );
        }
    }

    @Override
    public List<Float> getEmbedding(UUID userId) {
        try {
            List<Points.RetrievedPoint> points =
                    client.retrieveAsync(
                            COLLECTION_NAME,
                            List.of(PointIdFactory.id(userId)),
                            true,
                            true,
                            null
                    ).get();

            log.info("Retrieved points: {}", points.size());
            log.info("Check userId = {}", userId);

            if (points.isEmpty()) {
                return null;
            }

            return points.get(0)
                    .getVectors()
                    .getVector()
                    .getDataList();

        } catch (Exception e) {
            throw new QdrantException(
                    "Cannot get embedding for user: " + userId,
                    e
            );
        }
    }

    @Override
    public void delete(UUID userId) {
        try {
            client.deleteAsync(
                    COLLECTION_NAME,
                    List.of(PointIdFactory.id(userId)),
                    null
            ).get();

        } catch (Exception e) {
            throw new QdrantException(
                    "Cannot delete embedding for user: " + userId,
                    e
            );
        }
    }

    @Override
    public UUID searchVectorEmbedding(List<Float> embedding) {
        try {
            Points.SearchPoints request =
                    Points.SearchPoints.newBuilder()
                            .setCollectionName(COLLECTION_NAME)
                            .addAllVector(embedding)
                            .setLimit(1)
                            .setScoreThreshold(threshold)
                            .build();

            List<Points.ScoredPoint> results =
                    client.searchAsync(request).get();

            if (results.isEmpty()) {
                return null;
            }

            return UUID.fromString(
                    results.get(0)
                            .getId()
                            .getUuid()
            );

        } catch (Exception e) {
            throw new QdrantException(
                    "Cannot search vector embedding",
                    e
            );
        }
    }
}
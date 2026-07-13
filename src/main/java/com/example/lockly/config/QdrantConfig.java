package com.example.lockly.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    @Bean
    public QdrantClient qdrantClient() {

        QdrantGrpcClient grpcClient =
                QdrantGrpcClient.newBuilder(
                        "qdrant",
                        6334,
                        false
                ).build();

        return new QdrantClient(grpcClient);
    }
}

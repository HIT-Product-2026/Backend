package com.example.lockly.config.minio;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class MinioConfig {

    @Bean
    @Primary
    public MinioClient minioClient(MinioProperties props) {

        log.debug("Connect to MinIO");

        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(
                        props.getAccessKey(),
                        props.getSecretKey()
                )
                .build();
    }


    @Bean
    @PublicMinio
    public MinioClient publicMinioClient(MinioProperties props) {

        MinioClient client = MinioClient.builder()
                .endpoint(props.getPublicEndpoint())
                .credentials(
                        props.getAccessKey(),
                        props.getSecretKey()
                )
                .build();

        log.info("Public MinIO client created");

        return client;
    }
}
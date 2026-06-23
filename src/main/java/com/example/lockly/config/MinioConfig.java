package com.example.lockly.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    @Bean
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
}
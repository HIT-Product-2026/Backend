package com.example.lockly.service.Impl;

import com.example.lockly.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl {

    MinioClient minioClient;
    MinioProperties props;

    // UPLOAD
    // Thiếu logic cho vào 1 nơi cụ thể
    public String uploadFile(MultipartFile file) throws Exception {

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(fileName)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1
                        )
                        .contentType(file.getContentType())
                        .build()
        );

        return fileName;
    }

    // DOWNLOAD
    public InputStream getFile(String fileName) throws Exception {

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(fileName)
                        .build()
        );
    }
}
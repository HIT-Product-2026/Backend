package com.example.lockly.service.Impl;

import com.example.lockly.config.MinioProperties;
import com.example.lockly.service.MinIOService;
import io.minio.*;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinIOServiceImpl implements MinIOService {

    private final MinioClient minioClient;
    private final MinioProperties props;

    public void saveFile(MultipartFile file, String objectName) throws Exception {
        try {
            InputStream is = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );


        } catch (Exception e) {
            // chỉ throw lại để service phía trên quyết định rollback DB nếu cần
            throw new RuntimeException("MinIO upload failed: " + objectName, e);
        }
    }

    public InputStream getFile(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(objectName)
                        .build()
        );
    }

    public void deleteFile(String objectName) {

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            // Không xóa được thì thôi
        }
    }

    // Hàm tạo ra 1 url cho phép lấy dữ liệu từ minIO mà không cần public storage
    public String generatePresignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET.GET)
                        .bucket(props.getBucketName())
                        .object(objectName)
                        .expiry(60 * 60)
                        .build()
        );
    }
}
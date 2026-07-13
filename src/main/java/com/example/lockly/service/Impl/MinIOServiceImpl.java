package com.example.lockly.service.Impl;

import com.example.lockly.config.MinioProperties;
import com.example.lockly.service.MinIOService;
import io.minio.*;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinIOServiceImpl implements MinIOService {

    private final MinioClient minioClient;
    private final MinioProperties props;

    public void saveFile(MultipartFile file, String objectName){
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

    @Override
    public InputStream getFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file from MinIO", e);
        }
    }


    @Override
    public MultipartFile getMultipartFile(String objectName) {

        log.info("[MinIO] Download objectName={}", objectName);

        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .build()
            );

            return new MockMultipartFile(
                    objectName,
                    objectName,
                    getContentType(objectName),
                    inputStream
            );

        } catch (Exception e) {
            throw new RuntimeException("Cannot read file from MinIO", e);
        }
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
    public String generatePresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET.GET)
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .expiry(60 * 60)
                            .build()
            );
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    // Help
    private String getContentType(String objectName) {
        String extension = objectName.substring(objectName.lastIndexOf(".") + 1)
                .toLowerCase();

        return switch (extension) {
            // Image
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";

            // Audio
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "m4a" -> "audio/mp4";

            // Video
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "mkv" -> "video/x-matroska";

            default -> "application/octet-stream";
        };
    }
}
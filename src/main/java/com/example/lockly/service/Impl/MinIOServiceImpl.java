package com.example.lockly.service.Impl;

import com.example.lockly.config.minio.MinioProperties;
import com.example.lockly.config.minio.PublicMinio;
import com.example.lockly.exception.nonRetryException.InvalidConfigurationException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.exception.nonRetryException.UnauthorizedException;
import com.example.lockly.exception.retryException.MinIOException;
import com.example.lockly.service.MinIOService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOServiceImpl implements MinIOService {

    @PublicMinio
    private final MinioClient publicMinioClient;

    private final MinioClient minioClient;
    private final MinioProperties props;

    @Override
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

        log.info("[MinIO] Download objectName={}", objectName);

        try {

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .build()
            );

        } catch (ErrorResponseException e) {

            String code = e.errorResponse().code();

            switch (code) {

                case "NoSuchKey" ->
                        throw new ResourceNotFoundException(
                                "Không tìm thấy tệp '" + objectName + "' trong MinIO.",
                                e
                        );

                case "NoSuchBucket" ->
                        throw new InvalidConfigurationException(
                                "Bucket '" + props.getBucketName() + "' không tồn tại.",
                                e
                        );

                case "XMinioInvalidObjectName",
                     "InvalidBucketName" ->
                        throw new InvalidConfigurationException(
                                "Object name hoặc bucket name không hợp lệ.",
                                e
                        );

                case "AccessDenied" ->
                        throw new UnauthorizedException(
                                "Không có quyền truy cập vào MinIO.",
                                e
                        );

                default ->
                        throw new RuntimeException(
                                "MinIO trả về lỗi: " + code,
                                e
                        );
            }

        } catch (IOException e) {

            throw new MinIOException(
                    "Không thể kết nối hoặc đọc dữ liệu từ MinIO.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Lỗi không xác định khi đọc tệp từ MinIO.",
                    e
            );
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

        } catch (ErrorResponseException e) {

            String code = e.errorResponse().code();

            switch (code) {

                case "NoSuchKey" ->
                        throw new ResourceNotFoundException(
                                "Không tìm thấy tệp '" + objectName + "' trong MinIO.",
                                e
                        );

                case "NoSuchBucket" ->
                        throw new InvalidConfigurationException(
                                "Bucket '" + props.getBucketName() + "' không tồn tại.",
                                e
                        );

                case "XMinioInvalidObjectName",
                     "InvalidBucketName" ->
                        throw new InvalidConfigurationException(
                                "Object name hoặc bucket name không hợp lệ.",
                                e
                        );

                case "AccessDenied" ->
                        throw new UnauthorizedException(
                                "Không có quyền truy cập vào MinIO.",
                                e
                        );

                default ->
                        throw new RuntimeException(
                                "MinIO trả về lỗi: " + code,
                                e
                        );
            }

        } catch (IOException e) {

            // Lỗi mạng / server -> có thể retry
            throw new MinIOException(
                    "Không thể kết nối hoặc đọc dữ liệu từ MinIO.",
                    e
            );

        }  catch (Exception e) {

            throw new RuntimeException(
                    "Lỗi không xác định khi đọc tệp từ MinIO.",
                    e
            );
        }
    }

    @Override
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
    @Override
    public String generatePresignedUrl(String objectName) {

        if (objectName == null)
            return null;

        try {

            log.info("props.publicEndpoint = {}", props.getPublicEndpoint());
            log.info("publicMinioClient = {}", publicMinioClient);

            String url = publicMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(props.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );

            log.info("Generated presigned URL = {}", url);
            log.info("publicMinioClient class = {}", publicMinioClient.getClass());

            return url;

        } catch (Exception e) {

            log.error(
                    "[MinIO] Cannot generate presigned url for {}",
                    objectName,
                    e
            );

            throw new RuntimeException(
                    "Cannot generate presigned url",
                    e
            );
        }
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
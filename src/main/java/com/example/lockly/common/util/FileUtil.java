package com.example.lockly.common.util;
import com.example.lockly.constant.ApiPath;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FileUtil{
    public static String getObjectNameFile(String prefix, UUID objectId){
        return prefix
                + "/"
                + objectId
                + "/"
                + UUID.randomUUID();
    }

    public static String getImageUrlApi(String prefix, UUID objectId){
        return ApiPath.API_NOW
                + "/"
                + prefix
                + "/"
                + objectId
                + "/image";
    }

    public static String extractObjectName(String url) {
        URI uri = URI.create(url);

        // /images/users/avatar/019f.../c8ee...
        String path = URLDecoder.decode(
                uri.getPath(),
                StandardCharsets.UTF_8
        );

        String[] parts = path.split("/", 3);

        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid MinIO URL");
        }

        return parts[2];
    }
}

package com.example.lockly.common.util;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class FileUtil{
    public static String getObjectNameFile(String prefix, String objectId, MultipartFile file){
        return prefix
                + "/"
                + objectId
                + "/"
                + UUID.randomUUID()
                + "_"
                + file.getOriginalFilename();
    }
}

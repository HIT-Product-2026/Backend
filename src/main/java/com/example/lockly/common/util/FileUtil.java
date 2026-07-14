package com.example.lockly.common.util;
import com.example.lockly.constant.ApiPath;
import org.springframework.web.multipart.MultipartFile;

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
}

package com.example.lockly.common.util;

import com.example.lockly.domain.dto.request.PostCursor;
import com.example.lockly.exception.nonRetryException.InvalidCursorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public class CursorUtil {

    private static final ObjectMapper mapper = new ObjectMapper();


    // Post Cursor

    public static String encode(PostCursor cursor){

        if (cursor == null)
            return null;

        try {
            String json = mapper.writeValueAsString(cursor);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        } catch (JsonProcessingException e){
            throw new InvalidCursorException(e);
        }
    }


    public static PostCursor decode(String token){

        if (token == null || token.isBlank()) {
            return new PostCursor(null, null);
        }

        try {
            String json = new String(
                    Base64.getUrlDecoder().decode(token),
                    StandardCharsets.UTF_8
            );

            return mapper.readValue(json, PostCursor.class);
        } catch (Exception e) {
            throw new InvalidCursorException(e);
        }
    }

    // Khác

}

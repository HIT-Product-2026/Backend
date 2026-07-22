package com.example.lockly.common.util;

import com.example.lockly.domain.dto.request.Cursor;
import com.example.lockly.exception.nonRetryException.InvalidCursorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public class CursorUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());


    // Post Cursor

    public static String encode(Cursor cursor){

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


    public static Cursor decode(String token){

        if (token == null || token.isBlank()) {
            return new Cursor(null, null);
        }

        try {
            String json = new String(
                    Base64.getUrlDecoder().decode(token),
                    StandardCharsets.UTF_8
            );

            return mapper.readValue(json, Cursor.class);
        } catch (Exception e) {
            throw new InvalidCursorException(e);
        }
    }

    // Khác

}

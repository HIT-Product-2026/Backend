package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.PostModeLocation;
import com.example.lockly.domain.entity.User;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface PostService {
    PostResponseDto createPost(CreatePostRequestDto request) throws Exception;
    InputStream getPostImage(UUID postId) throws Exception;
    PostResponseDto getPostById(UUID postId);
    List<PostResponseDto> getPostByUserId(User user, int pageNumber);
    void updateModeLocationPostById(UUID postId, PostModeLocation modeLocation);
    LocationPostResponseDto getLocationPost(UUID postId);
    void sendEmoji(ReactEmojiToPostRequestDto request);
}

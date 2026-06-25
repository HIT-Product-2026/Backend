package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.PostModeLocation;
import com.example.lockly.domain.entity.User;

import java.io.InputStream;
import java.util.List;

public interface PostService {
    PostResponseDto createPost(CreatePostRequestDto request) throws Exception;
    InputStream getPostImage(String postId) throws Exception;
    PostResponseDto getPostById(String postId);
    List<PostResponseDto> getPostByUserId(User user, int pageNumber);
    void updateModeLocationPostById(String postId, PostModeLocation modeLocation);
    LocationPostResponseDto getLocationPost(String postId);
    void sendEmoji(ReactEmojiToPostRequestDto request);
}

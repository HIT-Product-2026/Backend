package com.example.lockly.service;

import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.response.PostResponseDto;

import java.io.InputStream;
import java.util.List;

public interface PostService {
    PostResponseDto createPost(CreatePostRequestDto request) throws Exception;
    InputStream getPostImage(String postId) throws Exception;
    PostResponseDto getPostById(String postId);
    List<PostResponseDto> getPostByUserId(String userId);
}

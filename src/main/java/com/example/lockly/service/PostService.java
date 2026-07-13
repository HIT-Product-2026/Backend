package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.create.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface PostService {
    PostResponseDto createPost(CreatePostRequestDto request) throws Exception;
    void sendEmoji(ReactEmojiToPostRequestDto request);

    InputStream getPostImage(UUID postId) throws Exception;
    PostResponseDto getPostById(UUID postId);
    List<PostResponseDto> getPostByUserId(UUID userId, int pageNumber);
    LocationPostResponseDto getLocationPost(UUID postId);
    List<EmojiPostResponseDto> getEmojiPosts(List<UUID> postIds);
    List<PostResponseDto> getFriendPosts(UUID userId, int pageNumber);

    void updateModeLocationPostById(UUID postId, PostModeLocation modeLocation);

    void dropEmoji(ReactEmojiToPostRequestDto request);
}

package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.create.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostDetailResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface PostService {
    PostResponseDto createPost(CreatePostRequestDto request);
    void sendEmoji(ReactEmojiToPostRequestDto request);

    String getPostImage(UUID postId) throws Exception;
    PostResponseDto getPostById(UUID postId);
    List<PostDetailResponseDto> getPostByUserId(UUID userId, String cursor);
    LocationPostResponseDto getLocationPost(UUID postId);
    List<EmojiPostResponseDto> getEmojiPosts(List<UUID> postIds);
    List<PostDetailResponseDto> getFriendPosts(User user, String cursor);

    void updateModeLocationPostById(UUID postId, PostModeLocation modeLocation);

    void dropEmoji(ReactEmojiToPostRequestDto request);
    void deletePost(UUID postId);
}

package com.example.lockly.service;

import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.util.List;
import java.util.UUID;

public interface UserBlockService {

    void blockUser(User blocker, UUID blockedId);

    void unblockUser(User blocker, UUID blockedId);

    List<UserSimpleResponseDto> getBlockedUsers(User blocker);
}
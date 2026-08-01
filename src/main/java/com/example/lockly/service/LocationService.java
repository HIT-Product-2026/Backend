package com.example.lockly.service;

import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.util.List;

public interface LocationService {
    boolean isDropRequest(Double longitude, Double latitude, User user);
    boolean isUnknownLocation(Double longitude, Double latitude, User user);
    String getProvinceFullName(Double latitude, Double longitude);
    String getWardFullName(Double latitude, Double longitude);
    List<LocationUserResponseDto> getLocationFriends(User user);
}
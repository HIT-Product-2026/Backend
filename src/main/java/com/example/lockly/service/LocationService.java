package com.example.lockly.service;

import com.example.lockly.domain.entity.main.User;

public interface LocationService {
    boolean isDropRequest(Double longitude, Double latitude, User user);
    boolean isUnknownLocation(Double longitude, Double latitude, User user);
    String getProvinceFullName(Double latitude, Double longitude);
}
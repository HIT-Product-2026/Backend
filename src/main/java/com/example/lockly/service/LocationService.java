package com.example.lockly.service;

public interface LocationService {
    boolean isDropRequest(Double longitude, Double latitude);
    boolean isUnknownLocation(Double longitude, Double latitude);
}
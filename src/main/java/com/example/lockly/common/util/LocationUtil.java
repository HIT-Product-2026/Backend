package com.example.lockly.common.util;

public class LocationUtil {

    private static final double R = 6371000; // meters

    // Khoảng cách tính bằng m
    public static double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2
    ) {

        // Chuyển sang radian
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Công thức Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Khoảng cách (m)
        return R * c;
    }
}
package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_NOW + "/location")
@Tag(name = "Location Controller", description = "API xử lý vị trí")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/province")
    @Operation(summary = "Lấy tên đầy đủ tỉnh/thành theo tọa độ")
    public ResponseEntity<ApiResponse<String>> getProvinceByLocation(

            @Parameter(description = "Vĩ độ (Latitude)", example = "21.028511")
            @RequestParam("latitude")
            @NotNull Double latitude,

            @Parameter(description = "Kinh độ (Longitude)", example = "105.804817")
            @RequestParam("longitude")
            @NotNull Double longitude
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Thành công",
                        locationService.getProvinceFullName(latitude, longitude)
                )
        );
    }
}
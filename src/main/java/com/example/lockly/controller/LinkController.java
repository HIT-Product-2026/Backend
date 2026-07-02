package com.example.lockly.controller;

import com.example.lockly.constant.ApiPath;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_V1 + "/link")
@Tag(name = "Link controller", description = "Quản lý ý nghĩa của các link")
public class LinkController {

    // Logic lấy profile
}

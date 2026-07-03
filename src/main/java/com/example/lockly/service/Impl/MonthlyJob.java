package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.Profile;
import com.example.lockly.repository.ProfileRepository;
import com.example.lockly.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyJob {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;


    @Scheduled(cron = "0 0 2 1 * ?")
    public void run() {
        int page = 0;
        int size = 500;

        Page<Profile> results;

        do {
            results = profileRepository.findAll(PageRequest.of(page, size));

            // Cập nhật danh hiệu

        } while (results.hasNext());
    }
}

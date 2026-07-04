package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.repository.main.ProfileRepository;
import com.example.lockly.service.AchievementService;
import com.example.lockly.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlyJob {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final AchievementService achievementService;


    // Cần thêm logic ghi nhớ tiến trình (Quartz, Spring Batch)
    @Scheduled(cron = "0 0 2 1 * ?")
    public void refreshAllUserCups() {
        int page = 0;
        int size = 500;

        Slice<Profile> results;

        do {
            results = profileRepository.findAll(PageRequest.of(page, size));

            for (Profile profile : results){
                // Cập nhật danh hiệu
                try {
                    achievementService.refreshProfileCups(profile);
                } catch (Exception e){
                    log.debug("Lỗi ở profile id: " + profile.getId());
                }
            }

            page++;

        } while (results.hasNext());
    }
}

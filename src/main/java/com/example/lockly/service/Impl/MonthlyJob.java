package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.repository.main.ProfileRepository;
import com.example.lockly.repository.main.UserRepository;
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
    private final UserRepository userRepository;
    private final AchievementService achievementService;


    // Cần thêm logic ghi nhớ tiến trình (Quartz, Spring Batch)
    @Scheduled(cron = "0 0 2 1 * ?")
    public void refreshAllUserCups() {
        int page = 0;
        int size = 500;

        Slice<User> results;

        do {
            results = userRepository.findAll(PageRequest.of(page, size));

            for (User user : results) {
                try {
                    achievementService.refreshProfileCups(user);
                } catch (Exception e) {
                    log.error("Lỗi khi refresh achievement cho userId={}", user.getId(), e);
                }
            }

            page++;

        } while (results.hasNext());
    }
}

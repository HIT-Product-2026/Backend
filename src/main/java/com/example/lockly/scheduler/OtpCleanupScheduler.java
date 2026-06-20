package com.example.lockly.scheduler;

import com.example.lockly.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpRepository otpRepository;

    /**
     * Mỗi giờ một lần, xóa tất cả OTP đã hết hạn để giữ DB sạch.
     * Cron: 0 0 * * * * = đầu mỗi giờ
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("[Scheduler] Bắt đầu xóa OTP hết hạn...");
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("[Scheduler] Đã xóa OTP hết hạn.");
    }
}
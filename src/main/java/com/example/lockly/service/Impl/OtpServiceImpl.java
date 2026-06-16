package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.OtpCode;
import com.example.lockly.domain.entity.OtpPurpose;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.repository.OtpRepository;
import com.example.lockly.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpServiceImpl implements OtpService {

    OtpRepository otpRepository;
    RestTemplate restTemplate;

    // ── Config từ application.properties ──────────────────────────────────────

    @NonFinal
    @Value("${otp.expiration-minutes:5}")
    int otpExpirationMinutes;

    @NonFinal
    @Value("${otp.resend-cooldown-seconds:60}")
    int resendCooldownSeconds;

    @NonFinal
    @Value("${zalo.oa.access-token:}")
    String zaloAccessToken;

    @NonFinal
    @Value("${zalo.oa.send-message-url:https://openapi.zalo.me/v3.0/oa/message/cs}")
    String zaloSendMessageUrl;

    // ── Constant ───────────────────────────────────────────────────────────────

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ══════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void sendOtp(String phoneNumber, OtpPurpose purpose) {

        // 1. Kiểm tra cooldown — tránh spam gửi lại
        checkResendCooldown(phoneNumber, purpose);

        // 2. Xóa toàn bộ OTP cũ của user này (DB luôn sạch)
        otpRepository.deleteAllByPhoneOrZaloIdAndPurpose(phoneNumber, purpose);

        // 3. Sinh OTP 6 số bằng SecureRandom
        String otpCode = generateOtp();

        // 4. Lưu vào DB
        OtpCode entity = OtpCode.builder()
                .phoneOrZaloId(phoneNumber)
                .otp(otpCode)
                .purpose(purpose)
                .expiredAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        otpRepository.save(entity);

        // 5. Gửi OTP qua Zalo
        sendViaZalo(phoneNumber, otpCode, purpose);

        log.info("[OTP] Đã gửi {} OTP tới {}", purpose, phoneNumber);
    }

    @Override
    @Transactional
    public void verifyOtp(String phoneNumber, String inputOtp, OtpPurpose purpose) {

        // 1. Tìm OTP hợp lệ (chưa dùng, chưa hết hạn)
        OtpCode entity = otpRepository
                .findValidOtp(phoneNumber, purpose, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn."));

        // 2. So sánh mã
        if (!entity.getOtp().equals(inputOtp)) {
            throw new BadRequestException("Mã OTP không chính xác.");
        }

        // 3. Đánh dấu đã dùng — one-time use
        entity.setUsed(true);
        otpRepository.save(entity);

        log.info("[OTP] Xác thực thành công {} OTP cho {}", purpose, phoneNumber);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Kiểm tra user có gửi lại OTP quá nhanh không.
     * Nếu OTP cũ còn trong DB và chưa đủ thời gian cooldown → throw exception.
     */
    private void checkResendCooldown(String phoneNumber, OtpPurpose purpose) {
        otpRepository.findValidOtp(phoneNumber, purpose, LocalDateTime.now())
                .ifPresent(existing -> {
                    long secondsElapsed = Duration.between(existing.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    long secondsLeft = resendCooldownSeconds - secondsElapsed;

                    if (secondsLeft > 0) {
                        throw new BadRequestException(
                                String.format("Vui lòng chờ %d giây trước khi gửi lại mã OTP.", secondsLeft)
                        );
                    }
                });
    }

    /**
     * Sinh OTP 6 chữ số ngẫu nhiên bằng SecureRandom (an toàn hơn Random).
     * Kết quả luôn đủ 6 chữ số (có padding 0 nếu cần).
     */
    private String generateOtp() {
        int otp = SECURE_RANDOM.nextInt(900000) + 100000; // 100000 → 999999
        return String.valueOf(otp);
    }

    /**
     * Gửi OTP qua Zalo OA API.
     *
     * Dev mode  : zalo.oa.access-token để trống → chỉ log ra console.
     * Production: điền access-token → gọi Zalo OA Send Message API thật.
     *
     * Zalo OA API yêu cầu user_id (Zalo User ID), không phải số điện thoại.
     * MVP: dùng phoneNumber trực tiếp làm key (sau này map sang zaloUserId từ bảng users).
     *
     * Tài liệu: https://developers.zalo.me/docs/official-account/gui-tin-nhan/gui-tin-nhan-tu-oa-den-nguoi-dung
     */
    private void sendViaZalo(String phoneNumber, String otpCode, OtpPurpose purpose) {
        String purposeText = (purpose == OtpPurpose.REGISTER) ? "đăng ký" : "đăng nhập";
        String message = buildOtpMessage(otpCode, purposeText);

        // ── DEV MODE: không có token → log ra console ──────────────────────
        if (zaloAccessToken == null || zaloAccessToken.isBlank()) {
            log.warn("──────────────────────────────────────────");
            log.warn("[OTP-DEV] SĐT   : {}", phoneNumber);
            log.warn("[OTP-DEV] Mục đích: {}", purpose);
            log.warn("[OTP-DEV] Mã OTP: {}", otpCode);
            log.warn("──────────────────────────────────────────");
            return;
        }

        // ── PRODUCTION MODE: gọi Zalo OA API ─────────────────────────────
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("access_token", zaloAccessToken);

            Map<String, Object> body = Map.of(
                    "recipient", Map.of("user_id", phoneNumber),
                    "message",   Map.of("text", message)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(zaloSendMessageUrl, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[OTP] Zalo API trả lỗi cho {}: {}", phoneNumber, response.getBody());
                throw new BadRequestException("Không thể gửi mã OTP qua Zalo. Vui lòng thử lại.");
            }

            log.info("[OTP] Gửi Zalo thành công tới {}", phoneNumber);

        } catch (BadRequestException e) {
            throw e; // re-throw để controller xử lý
        } catch (Exception e) {
            log.error("[OTP] Lỗi khi gọi Zalo API: {}", e.getMessage());
            throw new BadRequestException("Không thể gửi mã OTP. Vui lòng thử lại sau.");
        }
    }

    /**
     * Tạo nội dung tin nhắn OTP gửi cho user.
     */
    private String buildOtpMessage(String otpCode, String purposeText) {
        return String.format(
                "[Lockly] Mã %s của bạn là: %s\n" +
                        "Mã có hiệu lực trong %d phút.\n" +
                        "Không chia sẻ mã này với bất kỳ ai.",
                purposeText, otpCode, otpExpirationMinutes
        );
    }
}
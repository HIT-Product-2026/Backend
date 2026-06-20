package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.OtpCode;
import com.example.lockly.domain.entity.OtpPurpose;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.repository.OtpRepository;
import com.example.lockly.service.EmailService;
import com.example.lockly.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpServiceImpl implements OtpService {

    OtpRepository otpRepository;
    EmailService emailService;

    @NonFinal
    @Value("${otp.expiration-minutes:5}")
    int otpExpirationMinutes;

    @NonFinal
    @Value("${otp.resend-cooldown-seconds:60}")
    int resendCooldownSeconds;

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public void sendOtp(String email, OtpPurpose purpose) {
        // 1. Kiểm tra cooldown
        checkResendCooldown(email, purpose);

        // 2. Xóa OTP cũ
        otpRepository.deleteAllByEmailAndPurpose(email, purpose);

        // 3. Sinh OTP 6 số
        String otpCode = generateOtp();

        // 4. Lưu vào DB
        OtpCode entity = OtpCode.builder()
                .email(email)
                .otp(otpCode)
                .purpose(purpose)
                .expiredAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        otpRepository.save(entity);

        // 5. Gửi về Gmail
        String purposeText = (purpose == OtpPurpose.REGISTER) ? "đăng ký" : "đặt lại mật khẩu";
        emailService.sendOtpEmail(email, otpCode, purposeText);

        log.info("[OTP] Đã gửi {} OTP tới {}", purpose, email);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String inputOtp, OtpPurpose purpose) {
        // 1. Tìm OTP hợp lệ
        OtpCode entity = otpRepository
                .findValidOtp(email, purpose, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn."));

        // 2. So sánh mã
        if (!entity.getOtp().equals(inputOtp)) {
            throw new BadRequestException("Mã OTP không chính xác.");
        }

        // 3. Đánh dấu đã dùng
        entity.setUsed(true);
        otpRepository.save(entity);

        log.info("[OTP] Xác thực thành công {} OTP cho {}", purpose, email);
    }


    private void checkResendCooldown(String email, OtpPurpose purpose) {
        otpRepository.findValidOtp(email, purpose, LocalDateTime.now())
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

    private String generateOtp() {
        int otp = SECURE_RANDOM.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
}
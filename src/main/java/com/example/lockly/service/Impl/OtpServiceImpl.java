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

    @NonFinal
    @Value("${otp.max-attempts:5}")
    int maxOtpAttempts;

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public void sendOtp(String email, OtpPurpose purpose) {
        checkResendCooldown(email, purpose);
        otpRepository.deleteAllByEmailAndPurpose(email, purpose);

        String otpCode = generateOtp();

        OtpCode entity = OtpCode.builder()
                .email(email)
                .otp(otpCode)
                .purpose(purpose)
                .expiredAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        otpRepository.save(entity);

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

        // 2. Kiểm tra đã vượt giới hạn chưa
        if (entity.getAttemptCount() >= maxOtpAttempts) {
            entity.setUsed(true);
            otpRepository.save(entity);
            throw new BadRequestException(
                    String.format("Bạn đã nhập sai OTP quá %d lần. Vui lòng yêu cầu mã mới.", maxOtpAttempts)
            );
        }

        // 3. So sánh mã
        if (!entity.getOtp().equals(inputOtp)) {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            otpRepository.save(entity);
            int attemptsLeft = maxOtpAttempts - entity.getAttemptCount();
            if (attemptsLeft <= 0) {
                entity.setUsed(true);
                otpRepository.save(entity);
                throw new BadRequestException(
                        String.format("Mã OTP không chính xác. Bạn đã nhập sai quá %d lần. Vui lòng yêu cầu mã mới.", maxOtpAttempts)
                );
            }
            throw new BadRequestException(
                    String.format("Mã OTP không chính xác. Còn %d lần thử.", attemptsLeft)
            );
        }

        // 4. Đánh dấu đã dùng
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
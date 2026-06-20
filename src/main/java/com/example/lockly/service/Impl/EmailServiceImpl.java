package com.example.lockly.service.Impl;

import com.example.lockly.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    JavaMailSender mailSender;

    @NonFinal
    @Value("${spring.mail.username}")
    String fromEmail;

    @NonFinal
    @Value("${otp.expiration-minutes:5}")
    int otpExpirationMinutes;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Lockly App");
            helper.setTo(toEmail);
            helper.setSubject("[Lockly] Mã xác nhận " + purpose);
            helper.setText(buildEmailHtml(otpCode, purpose), true); // true = HTML

            mailSender.send(message);
            log.info("[EMAIL] Đã gửi OTP {} tới {}", purpose, toEmail);

        } catch (MessagingException e) {
            log.error("[EMAIL] Gửi email thất bại tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        } catch (Exception e) {
            log.error("[EMAIL] Lỗi không xác định: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    /**
     * Nội dung email dạng HTML — đẹp, rõ ràng cho người dùng.
     */
    private String buildEmailHtml(String otpCode, String purpose) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 32px; border: 1px solid #e0e0e0; border-radius: 12px;">
                    <h2 style="color: #333; text-align: center;">🔐 Lockly</h2>
                    <p style="color: #555; font-size: 15px;">Xin chào,</p>
                    <p style="color: #555; font-size: 15px;">
                        Đây là mã xác nhận <strong>%s</strong> của bạn:
                    </p>
                    <div style="text-align: center; margin: 24px 0;">
                        <span style="
                            display: inline-block;
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 12px;
                            color: #4A90E2;
                            background: #f0f6ff;
                            padding: 16px 32px;
                            border-radius: 8px;
                        ">%s</span>
                    </div>
                    <p style="color: #888; font-size: 13px; text-align: center;">
                        Mã có hiệu lực trong <strong>%d phút</strong>.<br/>
                        Không chia sẻ mã này với bất kỳ ai.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;"/>
                    <p style="color: #bbb; font-size: 12px; text-align: center;">
                        Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.
                    </p>
                </div>
                """.formatted(purpose, otpCode, otpExpirationMinutes);
    }
}
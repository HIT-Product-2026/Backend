package com.example.lockly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Số điện thoại hoặc Zalo ID của người dùng
     * Dùng làm "key" để tra cứu OTP
     */
    @Column(name = "phone_or_zalo_id", nullable = false, length = 50)
    private String phoneOrZaloId;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    /**
     * Loại OTP: REGISTER hoặc LOGIN
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private OtpPurpose purpose;

    /**
     * OTP hết hạn sau bao lâu (mặc định 5 phút)
     */
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    /**
     * Đã dùng OTP này chưa (tránh dùng lại)
     */
    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
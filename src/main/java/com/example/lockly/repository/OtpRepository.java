package com.example.lockly.repository;

import com.example.lockly.domain.entity.OtpCode;
import com.example.lockly.domain.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {

    /**
     * Lấy OTP mới nhất, chưa dùng, chưa hết hạn của một email
     */
    @Query("""
        SELECT o FROM OtpCode o
        WHERE o.email = :email
          AND o.purpose = :purpose
          AND o.used = false
          AND o.expiredAt > :now
        ORDER BY o.createdAt DESC
        LIMIT 1
    """)
    Optional<OtpCode> findValidOtp(
            @Param("email") String email,
            @Param("purpose") OtpPurpose purpose,
            @Param("now") LocalDateTime now
    );

    /**
     * Xóa toàn bộ OTP cũ của 1 email trước khi gửi mã mới
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.email = :email AND o.purpose = :purpose")
    void deleteAllByEmailAndPurpose(
            @Param("email") String email,
            @Param("purpose") OtpPurpose purpose
    );

    /**
     * Xóa các OTP đã hết hạn — dùng trong Cron Job cleanup
     */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiredAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
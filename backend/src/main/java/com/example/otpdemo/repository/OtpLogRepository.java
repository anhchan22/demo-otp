package com.example.otpdemo.repository;

import com.example.otpdemo.entity.OtpLog;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.enums.OtpStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpLogRepository extends JpaRepository<OtpLog, Long> {
        List<OtpLog> findByUserIdAndPurposeAndStatus(Long userId, OtpPurpose purpose, OtpStatus status);

        Optional<OtpLog> findTopByUserIdAndPurposeAndStatusOrderByCreatedAtDesc(
                        Long userId, OtpPurpose purpose, OtpStatus status);

        long countByUserIdAndCreatedAtAfter(Long userId, java.time.Instant createdAt);

        @Query(value = "SELECT * FROM otp_log WHERE challenge_id = :challengeId FOR UPDATE", nativeQuery = true)
        Optional<OtpLog> findByChallengeIdForUpdate(@Param("challengeId") String challengeId);

        @Query(value = "SELECT * FROM otp_log WHERE user_id = :userId AND purpose = :#{#purpose.name()} AND status = :#{#status.name()} ORDER BY verified_at DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
        Optional<OtpLog> findTopByUserIdAndPurposeAndStatusOrderByVerifiedAtDesc(
                        @Param("userId") Long userId, @Param("purpose") OtpPurpose purpose, @Param("status") OtpStatus status);
}

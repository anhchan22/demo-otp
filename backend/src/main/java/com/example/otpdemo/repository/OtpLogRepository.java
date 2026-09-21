package com.example.otpdemo.repository;

import com.example.otpdemo.entity.OtpLog;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.enums.OtpStatus;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpLogRepository extends JpaRepository<OtpLog, Long> {
    List<OtpLog> findByUserIdAndPurposeAndStatus(Long userId, OtpPurpose purpose, OtpStatus status);
    Optional<OtpLog> findTopByUserIdAndPurposeAndStatusOrderByCreatedAtDesc(
            Long userId, OtpPurpose purpose, OtpStatus status);
    long countByUserIdAndCreatedAtAfter(Long userId, java.time.Instant createdAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OtpLog o where o.challengeId = :challengeId")
    Optional<OtpLog> findByChallengeIdForUpdate(@Param("challengeId") String challengeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpLog> findTopByUserIdAndPurposeAndStatusOrderByVerifiedAtDesc(
            Long userId, OtpPurpose purpose, OtpStatus status);
}

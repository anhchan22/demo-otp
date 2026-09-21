package com.example.otpdemo.service;

import com.example.otpdemo.dto.response.SendOtpResponse;
import com.example.otpdemo.dto.response.VerifyOtpResponse;
import com.example.otpdemo.entity.OtpLog;
import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.enums.OtpStatus;
import com.example.otpdemo.enums.UserStatus;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import com.example.otpdemo.repository.OtpLogRepository;
import com.example.otpdemo.repository.UserRepository;
import com.example.otpdemo.service.sender.OtpSenderRouter;
import com.example.otpdemo.util.MaskingUtils;
import com.example.otpdemo.util.OtpGenerator;
import com.example.otpdemo.util.OtpHasher;
import com.example.otpdemo.util.OtpRateLimiter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OtpService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OtpService.class);

    private final UserRepository userRepository;
    private final OtpLogRepository otpLogRepository;
    private final OtpGenerator otpGenerator;
    private final OtpHasher otpHasher;
    private final OtpSenderRouter otpSender;
    private final OtpRateLimiter otpRateLimiter;

    @org.springframework.beans.factory.annotation.Value("${otp.ttl-seconds:300}")
    private long otpTtlSeconds;
    @org.springframework.beans.factory.annotation.Value("${otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;
    @org.springframework.beans.factory.annotation.Value("${otp.rate-limit.window-seconds:600}")
    private long rateLimitWindowSeconds;
    @org.springframework.beans.factory.annotation.Value("${otp.rate-limit.max-send-per-window:5}")
    private long maxSendPerWindow;
    @org.springframework.beans.factory.annotation.Value("${otp.max-attempts:5}")
    private int maxAttempts;
    @org.springframework.beans.factory.annotation.Value("${otp.dev-preview:false}")
    private boolean devPreview;

    @Transactional
    public SendOtpResponse sendOtp(Long userId, OtpPurpose purpose, OtpChannel channel, String clientIp) {
        otpRateLimiter.check("send:" + clientIp);
        User user = findUser(userId);
        if (purpose == OtpPurpose.VERIFY_ACCOUNT && user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        }
        validateChannel(user, channel);
        Instant now = Instant.now();
        validateRateLimit(userId, now);
        validateCooldown(userId, purpose, now);
        SendOtpResponse response = createAndSend(user, purpose, channel, now);
        LOGGER.info("otp.sent userId={} purpose={} channel={} challengeId={}",
                userId, purpose, channel, response.getChallengeId());
        return response;
    }

    @Transactional
    public SendOtpResponse resendOtp(String challengeId, Long userId, OtpPurpose purpose, String clientIp) {
        otpRateLimiter.check("resend:" + clientIp);
        OtpLog currentOtp = otpLogRepository.findByChallengeIdForUpdate(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_NOT_FOUND));
        validateOwnership(currentOtp, userId, purpose);
        validateResendable(currentOtp);

        Instant now = Instant.now();
        validateRateLimit(userId, now);
        validateCooldown(userId, currentOtp.getPurpose(), now);
        invalidatePendingOtps(userId, currentOtp.getPurpose(), now);
        SendOtpResponse response = createAndSend(currentOtp.getUser(), currentOtp.getPurpose(), currentOtp.getChannel(), now);
        LOGGER.info("otp.resent userId={} purpose={} channel={} challengeId={}",
                userId, currentOtp.getPurpose(), currentOtp.getChannel(), response.getChallengeId());
        return response;
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(String challengeId, String rawOtp, Long userId, OtpPurpose purpose) {
        OtpLog otpLog = otpLogRepository.findByChallengeIdForUpdate(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_NOT_FOUND));
        validateOwnership(otpLog, userId, purpose);
        validateCurrentStatus(otpLog);

        Instant now = Instant.now();
        if (!now.isBefore(otpLog.getExpiresAt())) {
            otpLog.setStatus(OtpStatus.EXPIRED);
            otpLogRepository.save(otpLog);
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        if (!otpHasher.matches(rawOtp, otpLog.getOtpHash())) {
            int attemptCount = otpLog.getAttemptCount() + 1;
            otpLog.setAttemptCount(attemptCount);
            if (attemptCount >= maxAttempts) {
                otpLog.setStatus(OtpStatus.BLOCKED);
                otpLogRepository.save(otpLog);
                LOGGER.warn("otp.blocked otpId={} userId={} purpose={} channel={}",
                        otpLog.getId(), otpLog.getUser().getId(), otpLog.getPurpose(), otpLog.getChannel());
                throw new BusinessException(ErrorCode.OTP_BLOCKED);
            }
            otpLogRepository.save(otpLog);
            LOGGER.warn("otp.invalid otpId={} userId={} purpose={} channel={} attempt={}",
                    otpLog.getId(), otpLog.getUser().getId(), otpLog.getPurpose(), otpLog.getChannel(), attemptCount);
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        otpLog.setStatus(OtpStatus.VERIFIED);
        otpLog.setVerifiedAt(now);
        otpLogRepository.save(otpLog);
        if (otpLog.getPurpose() == OtpPurpose.VERIFY_ACCOUNT
                && otpLog.getUser().getStatus() == UserStatus.UNVERIFIED) {
            otpLog.getUser().setStatus(UserStatus.ACTIVE);
            userRepository.save(otpLog.getUser());
        }
        LOGGER.info("otp.verified otpId={} userId={} purpose={} channel={}",
                otpLog.getId(), otpLog.getUser().getId(), otpLog.getPurpose(), otpLog.getChannel());
        return VerifyOtpResponse.builder()
                .verified(true)
                .purpose(otpLog.getPurpose())
                .remainingAttempts(maxAttempts - otpLog.getAttemptCount())
                .build();
    }

    private SendOtpResponse createAndSend(User user, OtpPurpose purpose, OtpChannel channel, Instant now) {
        invalidatePendingOtps(user.getId(), purpose, now);

        String rawOtp = otpGenerator.generate();
        OtpLog otpLog = new OtpLog();
        otpLog.setUser(user);
        otpLog.setOtpHash(otpHasher.hash(rawOtp));
        otpLog.setPurpose(purpose);
        otpLog.setChannel(channel);
        otpLog.setStatus(OtpStatus.PENDING);
        otpLog.setExpiresAt(now.plusSeconds(otpTtlSeconds));
        OtpLog savedOtp = otpLogRepository.save(otpLog);

        try {
            otpSender.send(user, rawOtp, purpose, channel);
        } catch (BusinessException exception) {
            if (!devPreview) {
                throw exception;
            }
            LOGGER.warn("[DEV ONLY] OTP provider failed; retaining challengeId={} for local verification. "
                            + "purpose={} channel={} otp={} code={}",
                    savedOtp.getChallengeId(), purpose, channel, rawOtp, exception.getErrorCode().getCode());
        }
        return SendOtpResponse.builder()
                .challengeId(savedOtp.getChallengeId())
                .channel(channel)
                .destination(maskDestination(user, channel))
                .expiresIn(otpTtlSeconds)
                .resendAfter(resendCooldownSeconds)
                .build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateChannel(User user, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL && (user.getEmail() == null || user.getEmail().isBlank())) {
            throw new BusinessException(ErrorCode.OTP_INVALID_CHANNEL);
        }
        if ((channel == OtpChannel.SMS || channel == OtpChannel.ZALO)
                && (user.getPhone() == null || user.getPhone().isBlank())) {
            throw new BusinessException(ErrorCode.OTP_INVALID_CHANNEL);
        }
    }

    private void validateRateLimit(Long userId, Instant now) {
        long requestCount = otpLogRepository.countByUserIdAndCreatedAtAfter(
                userId, now.minusSeconds(rateLimitWindowSeconds));
        if (requestCount >= maxSendPerWindow) {
            throw new BusinessException(ErrorCode.OTP_TOO_MANY_REQUESTS);
        }
    }

    private void validateCooldown(Long userId, OtpPurpose purpose, Instant now) {
        otpLogRepository.findTopByUserIdAndPurposeAndStatusOrderByCreatedAtDesc(
                        userId, purpose, OtpStatus.PENDING)
                .ifPresent(latestOtp -> {
                    long elapsedSeconds = Duration.between(latestOtp.getCreatedAt(), now).getSeconds();
                    if (elapsedSeconds < resendCooldownSeconds) {
                        throw new BusinessException(ErrorCode.OTP_RESEND_COOLDOWN);
                    }
                });
    }

    private void invalidatePendingOtps(Long userId, OtpPurpose purpose, Instant now) {
        List<OtpLog> pendingOtps = otpLogRepository.findByUserIdAndPurposeAndStatus(
                userId, purpose, OtpStatus.PENDING);
        pendingOtps.forEach(otpLog -> {
            otpLog.setStatus(OtpStatus.INVALIDATED);
            otpLog.setInvalidatedAt(now);
        });
        otpLogRepository.saveAll(pendingOtps);
    }

    private void validateResendable(OtpLog otpLog) {
        if (otpLog.getStatus() == OtpStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.OTP_ALREADY_USED);
        }
        if (otpLog.getStatus() == OtpStatus.INVALIDATED) {
            throw new BusinessException(ErrorCode.OTP_INVALIDATED);
        }
        if (otpLog.getStatus() == OtpStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.OTP_BLOCKED);
        }
    }

    private void validateCurrentStatus(OtpLog otpLog) {
        if (otpLog.getStatus() == OtpStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.OTP_ALREADY_USED);
        }
        if (otpLog.getStatus() == OtpStatus.INVALIDATED) {
            throw new BusinessException(ErrorCode.OTP_INVALIDATED);
        }
        if (otpLog.getStatus() == OtpStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.OTP_BLOCKED);
        }
        if (otpLog.getStatus() == OtpStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }
    }

    private void validateOwnership(OtpLog otpLog, Long userId, OtpPurpose purpose) {
        if (!otpLog.getUser().getId().equals(userId) || otpLog.getPurpose() != purpose) {
            throw new BusinessException(ErrorCode.OTP_OWNERSHIP);
        }
    }

    private String maskDestination(User user, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL) {
            return MaskingUtils.maskEmail(user.getEmail());
        }
        return MaskingUtils.maskPhone(user.getPhone());
    }
}

package com.example.otpdemo.util;

import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtpRateLimiter {

    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();
    private final long windowSeconds;
    private final long maxRequests;

    public OtpRateLimiter(
            @Value("${otp.ip-rate-limit.window-seconds:600}") long windowSeconds,
            @Value("${otp.ip-rate-limit.max-requests:20}") long maxRequests) {
        this.windowSeconds = windowSeconds;
        this.maxRequests = maxRequests;
    }

    public void check(String key) {
        long now = Instant.now().getEpochSecond();
        windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAt() >= windowSeconds) {
                return new Window(now, 1);
            }
            if (current.count() >= maxRequests) {
                throw new BusinessException(ErrorCode.OTP_TOO_MANY_REQUESTS);
            }
            return new Window(current.startedAt(), current.count() + 1);
        });
    }

    private record Window(long startedAt, long count) {
    }
}

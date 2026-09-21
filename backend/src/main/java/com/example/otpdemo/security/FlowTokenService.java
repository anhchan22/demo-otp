package com.example.otpdemo.security;

import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class FlowTokenService {
    public static final String COOKIE_NAME = "flow_token";

    private final byte[] secret;
    private final long ttlSeconds;
    private final boolean cookieSecure;

    public FlowTokenService(
            @Value("${app.flow-secret}") String secret,
            @Value("${app.flow-ttl-seconds:900}") long ttlSeconds,
            @Value("${app.cookie-secure:false}") boolean cookieSecure) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("FLOW_SECRET phải có ít nhất 32 ký tự");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
        this.cookieSecure = cookieSecure;
    }

    public String createToken(Long userId, OtpPurpose purpose) {
        return createToken(userId, purpose, false);
    }

    public String createToken(Long userId, OtpPurpose purpose, boolean verified) {
        String payload = userId + "|" + purpose.name() + "|"
                + Instant.now().plusSeconds(ttlSeconds).getEpochSecond() + "|" + verified;
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(encoded);
    }

    public FlowClaims requireClaims(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new BusinessException(ErrorCode.FLOW_INVALID);
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return parseToken(cookie.getValue());
            }
        }
        throw new BusinessException(ErrorCode.FLOW_INVALID);
    }

    public FlowClaims parseToken(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2 || !MessageDigest.isEqual(
                    sign(parts[0]).getBytes(StandardCharsets.UTF_8),
                    parts[1].getBytes(StandardCharsets.UTF_8))) {
                throw new BusinessException(ErrorCode.FLOW_INVALID);
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\|", -1);
            if (values.length != 4) {
                throw new BusinessException(ErrorCode.FLOW_INVALID);
            }
            long expiresAt = Long.parseLong(values[2]);
            if (Instant.now().getEpochSecond() >= expiresAt) {
                throw new BusinessException(ErrorCode.FLOW_EXPIRED);
            }
            return new FlowClaims(
                    Long.valueOf(values[0]), OtpPurpose.valueOf(values[1]), Boolean.parseBoolean(values[3]));
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.FLOW_INVALID);
        }
    }

    public ResponseCookie buildCookie(String token) {
        return cookie(token, Duration.ofSeconds(ttlSeconds));
    }

    public ResponseCookie clearCookie() {
        return cookie("", Duration.ZERO);
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api")
                .maxAge(maxAge)
                .build();
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể ký flow token", exception);
        }
    }

    public record FlowClaims(Long userId, OtpPurpose purpose, boolean verified) {
    }
}

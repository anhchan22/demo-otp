package com.example.otpdemo.util;

import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtpHasher {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final String secret;

    public OtpHasher(@Value("${otp.hmac-secret:}") String secret) {
        this.secret = secret;
    }

    public String hash(String otp) {
        if (secret.isBlank()) {
            throw new BusinessException(ErrorCode.OTP_SECRET_NOT_CONFIGURED);
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return toHex(mac.doFinal(otp.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("Không thể tạo HMAC cho OTP", exception);
        }
    }

    public boolean matches(String otp, String expectedHash) {
        return MessageDigest.isEqual(
                hash(otp).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format("%02x", value));
        }
        return result.toString();
    }
}

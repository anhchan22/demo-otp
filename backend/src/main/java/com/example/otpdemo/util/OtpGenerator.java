package com.example.otpdemo.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {
    private static final int OTP_BOUND = 1_000_000;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return String.format("%06d", secureRandom.nextInt(OTP_BOUND));
    }
}

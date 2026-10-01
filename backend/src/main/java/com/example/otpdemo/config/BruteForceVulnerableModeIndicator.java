package com.example.otpdemo.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Hiển thị cảnh báo khi ứng dụng chạy ở chế độ brute-force vulnerable.
 * Bean này chỉ được kích hoạt khi profile "bruteforce-vulnerable" đang active.
 */
@Component
@Profile("bruteforce-vulnerable")
public class BruteForceVulnerableModeIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BruteForceVulnerableModeIndicator.class);

    @PostConstruct
    public void warn() {
        String banner = """
                
                ╔══════════════════════════════════════════════════════════════╗
                ║                                                            ║
                ║   ⚠️  BRUTE-FORCE VULNERABLE MODE ĐANG BẬT ⚠️              ║
                ║                                                            ║
                ║   Toàn bộ cơ chế chống brute-force đã bị VÔ HIỆU HÓA:     ║
                ║   • Rate limiting: TẮT                                     ║
                ║   • Max attempts per OTP: TẮT                              ║
                ║   • IP rate limiting: TẮT                                  ║
                ║   • Resend cooldown: TẮT                                   ║
                ║                                                            ║
                ║   CHỈ SỬ DỤNG CHO MỤC ĐÍCH DEMO / HỌC TẬP                ║
                ║   KHÔNG SỬ DỤNG TRONG PRODUCTION!                          ║
                ║                                                            ║
                ╚══════════════════════════════════════════════════════════════╝
                """;
        LOGGER.warn(banner);
    }
}

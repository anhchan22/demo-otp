package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;

/** Retained as a reference adapter; the active sender is EmailOtpSender. */
public class NoopOtpSender implements OtpSender {
    @Override
    public OtpChannel channel() {
        return OtpChannel.ZALO;
    }

    @Override
    public void send(User user, String rawOtp, OtpPurpose purpose, OtpChannel channel) {
        // Delivery is intentionally deferred to the Phase 4 email integration.
    }
}

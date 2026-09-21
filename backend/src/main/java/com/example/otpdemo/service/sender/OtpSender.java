package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;

public interface OtpSender {
    OtpChannel channel();

    default boolean isAvailable() {
        return true;
    }

    default boolean isSandbox() {
        return false;
    }

    default String unavailableReason() {
        return null;
    }

    void send(User user, String rawOtp, OtpPurpose purpose, OtpChannel channel);
}

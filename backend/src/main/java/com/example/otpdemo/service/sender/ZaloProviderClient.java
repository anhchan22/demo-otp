package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpPurpose;

public interface ZaloProviderClient {
    boolean isAvailable();

    String unavailableReason();

    void send(User user, String rawOtp, OtpPurpose purpose);
}

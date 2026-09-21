package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ZaloOtpSender implements OtpSender {

    private final ZaloProviderClient providerClient;

    public ZaloOtpSender(ZaloProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    @Override
    public OtpChannel channel() {
        return OtpChannel.ZALO;
    }

    @Override
    public boolean isAvailable() {
        return providerClient.isAvailable();
    }

    @Override
    public String unavailableReason() {
        return providerClient.unavailableReason();
    }

    @Override
    public void send(User user, String rawOtp, OtpPurpose purpose, OtpChannel channel) {
        if (channel != OtpChannel.ZALO || user.getPhone() == null || user.getPhone().isBlank()) {
            throw new BusinessException(ErrorCode.OTP_INVALID_CHANNEL);
        }
        if (!isAvailable()) {
            throw new BusinessException(ErrorCode.ZALO_PROVIDER_NOT_CONFIGURED);
        }
        providerClient.send(user, rawOtp, purpose);
    }
}

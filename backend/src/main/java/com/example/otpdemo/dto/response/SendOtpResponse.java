package com.example.otpdemo.dto.response;

import com.example.otpdemo.enums.OtpChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendOtpResponse {
    private String challengeId;
    private OtpChannel channel;
    private String destination;
    private long expiresIn;
    private long resendAfter;
}

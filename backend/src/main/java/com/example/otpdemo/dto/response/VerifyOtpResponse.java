package com.example.otpdemo.dto.response;

import com.example.otpdemo.enums.OtpPurpose;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyOtpResponse {
    private boolean verified;
    private OtpPurpose purpose;
    private Integer remainingAttempts;
}

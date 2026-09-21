package com.example.otpdemo.dto.request;

import com.example.otpdemo.enums.OtpChannel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendOtpRequest {
    @NotNull(message = "Kênh gửi OTP không được để trống")
    private OtpChannel channel;
}

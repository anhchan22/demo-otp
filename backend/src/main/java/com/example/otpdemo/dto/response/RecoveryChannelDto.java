package com.example.otpdemo.dto.response;

import com.example.otpdemo.enums.OtpChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecoveryChannelDto {
    private OtpChannel channel;
    private String destination;
}

package com.example.otpdemo.dto.response;

import com.example.otpdemo.enums.OtpChannel;

public record OtpChannelAvailabilityDto(
        OtpChannel channel,
        boolean available,
        boolean sandbox,
        String destination,
        String reason
) {
}

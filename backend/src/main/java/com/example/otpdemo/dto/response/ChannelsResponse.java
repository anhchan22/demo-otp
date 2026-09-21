package com.example.otpdemo.dto.response;

import java.util.List;

public record ChannelsResponse(List<OtpChannelAvailabilityDto> channels) {
}

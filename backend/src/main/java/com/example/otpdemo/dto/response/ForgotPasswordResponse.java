package com.example.otpdemo.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotPasswordResponse {
    private List<RecoveryChannelDto> channels;
}

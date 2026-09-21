package com.example.otpdemo.exception;

import com.example.otpdemo.dto.response.RecoveryChannelDto;
import java.util.List;

public class UnverifiedAccountException extends BusinessException {
    private final Long userId;
    private final List<RecoveryChannelDto> channels;

    public UnverifiedAccountException(Long userId, List<RecoveryChannelDto> channels) {
        super(ErrorCode.ACCOUNT_NOT_VERIFIED);
        this.userId = userId;
        this.channels = channels;
    }

    public Long getUserId() {
        return userId;
    }

    public List<RecoveryChannelDto> getChannels() {
        return channels;
    }
}

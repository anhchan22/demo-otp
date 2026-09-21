package com.example.otpdemo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String code,
        String message,
        String traceId,
        List<RecoveryChannelDto> channels
) {
    public static ApiErrorResponse of(String code, String message, String traceId) {
        return new ApiErrorResponse(code, message, traceId, null);
    }
}

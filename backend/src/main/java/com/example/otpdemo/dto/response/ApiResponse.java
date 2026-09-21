package com.example.otpdemo.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.UUID;

public record ApiResponse<T>(
        String code,
        String message,
        String traceId,
        @JsonUnwrapped T payload
) {
    public static <T> ApiResponse<T> success(String message, T payload) {
        return new ApiResponse<>("SUCCESS", message, UUID.randomUUID().toString(), payload);
    }
}

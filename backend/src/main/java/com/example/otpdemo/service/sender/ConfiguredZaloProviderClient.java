package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfiguredZaloProviderClient implements ZaloProviderClient {

    private final boolean enabled;
    private final String provider;
    private final String oaId;
    private final String accessToken;
    private final String templateId;

    public ConfiguredZaloProviderClient(
            @Value("${zalo.enabled:false}") boolean enabled,
            @Value("${zalo.provider:esms}") String provider,
            @Value("${zalo.oa-id:}") String oaId,
            @Value("${zalo.access-token:}") String accessToken,
            @Value("${zalo.template-id:}") String templateId) {
        this.enabled = enabled;
        this.provider = provider;
        this.oaId = oaId;
        this.accessToken = accessToken;
        this.templateId = templateId;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String unavailableReason() {
        if (!enabled) {
            return "Zalo provider đang tắt";
        }
        if (!"esms".equalsIgnoreCase(provider) && !"oa".equalsIgnoreCase(provider)) {
            return "Zalo provider không được hỗ trợ";
        }
        if (oaId.isBlank() || accessToken.isBlank() || templateId.isBlank()) {
            return "Cần cấu hình OA, access token và template ID";
        }
        return "Zalo provider client đang chờ tích hợp API";
    }

    @Override
    public void send(User user, String rawOtp, OtpPurpose purpose) {
        throw new BusinessException(ErrorCode.ZALO_PROVIDER_NOT_CONFIGURED);
    }
}

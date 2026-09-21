package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EsmsSmsOtpSender implements OtpSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsmsSmsOtpSender.class);

    private final RestClient restClient;
    private final boolean enabled;
    private final String apiKey;
    private final String secretKey;
    private final String brandName;
    private final boolean sandbox;
    private final boolean devPreview;

    public EsmsSmsOtpSender(
            @Value("${esms.enabled:false}") boolean enabled,
            @Value("${esms.api-url:https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/}") String apiUrl,
            @Value("${esms.api-key:}") String apiKey,
            @Value("${esms.secret-key:}") String secretKey,
            @Value("${esms.brand-name:OTP Demo}") String brandName,
            @Value("${esms.sandbox:true}") boolean sandbox,
            @Value("${otp.dev-preview:false}") boolean devPreview) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.brandName = brandName;
        this.sandbox = sandbox;
        this.devPreview = devPreview;
        this.restClient = RestClient.builder().baseUrl(apiUrl).build();
    }

    @Override
    public OtpChannel channel() {
        return OtpChannel.SMS;
    }

    @Override
    public boolean isAvailable() {
        return localPreviewEnabled() || enabled && hasCredentials();
    }

    @Override
    public boolean isSandbox() {
        return sandbox;
    }

    @Override
    public String unavailableReason() {
        return "eSMS chưa được cấu hình";
    }

    @Override
    public void send(User user, String rawOtp, OtpPurpose purpose, OtpChannel channel) {
        if (channel != OtpChannel.SMS || user.getPhone() == null || user.getPhone().isBlank()) {
            throw new BusinessException(ErrorCode.OTP_INVALID_CHANNEL);
        }
        if (localPreviewEnabled() && !hasCredentials()) {
            LOGGER.warn("[DEV ONLY] SMS Sandbox local preview userId={} purpose={} otp={}",
                    user.getId(), purpose, rawOtp);
            return;
        }
        if (!enabled || !hasCredentials()) {
            throw new BusinessException(ErrorCode.SMS_PROVIDER_NOT_CONFIGURED);
        }
        if (sandbox && devPreview) {
            LOGGER.warn("[DEV ONLY] SMS Sandbox OTP userId={} purpose={} otp={}",
                    user.getId(), purpose, rawOtp);
        }

        String requestBody = "{"
                + "\"ApiKey\":" + jsonValue(apiKey) + ","
                + "\"SecretKey\":" + jsonValue(secretKey) + ","
                + "\"Phone\":" + jsonValue(user.getPhone()) + ","
                + "\"Content\":" + jsonValue(contentFor(user, rawOtp, purpose)) + ","
                + "\"Brandname\":" + jsonValue(brandName) + ","
                + "\"SmsType\":2,"
                + "\"Sandbox\":" + (sandbox ? 1 : 0)
                + "}";

        try {
            String responseBody = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            validateResponse(responseBody);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }

    private void validateResponse(String responseBody) {
        Matcher matcher = Pattern.compile("\\\"CodeResult\\\"\\s*:\\s*\\\"?(\\d+)").matcher(responseBody == null ? "" : responseBody);
        if (!matcher.find() || !"100".equals(matcher.group(1))) {
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }

    private String contentFor(User user, String rawOtp, OtpPurpose purpose) {
        String name = user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername()
                : user.getFullName();
        String action = purpose == OtpPurpose.VERIFY_ACCOUNT
                ? "xác thực tài khoản"
                : "đặt lại mật khẩu";
        return "OTP Demo: " + name + ", ma OTP " + rawOtp
                + " de " + action + ". Ma co hieu luc trong 5 phut.";
    }

    private boolean localPreviewEnabled() {
        return sandbox && devPreview;
    }

    private boolean hasCredentials() {
        return !apiKey.isBlank() && !secretKey.isBlank();
    }

    private String jsonValue(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                + "\"";
    }
}

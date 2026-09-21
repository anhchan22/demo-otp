package com.example.otpdemo.controller;

import com.example.otpdemo.dto.request.ResendOtpRequest;
import com.example.otpdemo.dto.request.SendOtpRequest;
import com.example.otpdemo.dto.request.VerifyOtpRequest;
import com.example.otpdemo.dto.response.ApiResponse;
import com.example.otpdemo.dto.response.SendOtpResponse;
import com.example.otpdemo.dto.response.VerifyOtpResponse;
import com.example.otpdemo.dto.response.ChannelsResponse;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.service.OtpChannelAvailabilityService;
import com.example.otpdemo.service.OtpService;
import com.example.otpdemo.security.FlowTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;
    private final OtpChannelAvailabilityService channelAvailabilityService;
    private final FlowTokenService flowTokenService;

    @PostMapping("/channels")
    public ApiResponse<ChannelsResponse> channels(HttpServletRequest request) {
        FlowTokenService.FlowClaims claims = flowTokenService.requireClaims(request);
        return ApiResponse.success("Đã lấy danh sách kênh OTP", new ChannelsResponse(
                channelAvailabilityService.getChannels(claims.userId(), claims.purpose())));
    }

    @PostMapping("/send")
    public ApiResponse<SendOtpResponse> send(
            @Valid @RequestBody SendOtpRequest request, HttpServletRequest httpRequest) {
        FlowTokenService.FlowClaims claims = flowTokenService.requireClaims(httpRequest);
        return ApiResponse.success("OTP đã được gửi", otpService.sendOtp(
                claims.userId(), claims.purpose(), request.getChannel(), httpRequest.getRemoteAddr()));
    }

    @PostMapping("/verify")
    public org.springframework.http.ResponseEntity<ApiResponse<VerifyOtpResponse>> verify(
            @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        FlowTokenService.FlowClaims claims = flowTokenService.requireClaims(httpRequest);
        VerifyOtpResponse response = otpService.verifyOtp(
                request.getChallengeId(), request.getOtp(), claims.userId(), claims.purpose());
        org.springframework.http.ResponseCookie cookie = claims.purpose() == OtpPurpose.RESET_PASSWORD
                ? flowTokenService.buildCookie(flowTokenService.createToken(
                        claims.userId(), claims.purpose(), true))
                : flowTokenService.clearCookie();
        return org.springframework.http.ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(ApiResponse.success("Xác thực OTP thành công", response));
    }

    @PostMapping("/resend")
    public ApiResponse<SendOtpResponse> resend(
            @Valid @RequestBody ResendOtpRequest request, HttpServletRequest httpRequest) {
        FlowTokenService.FlowClaims claims = flowTokenService.requireClaims(httpRequest);
        return ApiResponse.success("OTP mới đã được gửi", otpService.resendOtp(
                request.getChallengeId(), claims.userId(), claims.purpose(), httpRequest.getRemoteAddr()));
    }
}

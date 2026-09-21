package com.example.otpdemo.controller;

import com.example.otpdemo.dto.request.RegisterRequest;
import com.example.otpdemo.dto.request.LoginRequest;
import com.example.otpdemo.dto.request.ForgotPasswordRequest;
import com.example.otpdemo.dto.request.ResetPasswordRequest;
import com.example.otpdemo.dto.response.ApiResponse;
import com.example.otpdemo.dto.response.ForgotPasswordResponse;
import com.example.otpdemo.dto.response.LoginResponse;
import com.example.otpdemo.dto.response.RegisterResponse;
import com.example.otpdemo.service.AuthService;
import com.example.otpdemo.security.JwtService;
import com.example.otpdemo.security.FlowTokenService;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.UnverifiedAccountException;
import com.example.otpdemo.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final FlowTokenService flowTokenService;

    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthService.RegisterResult result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie", flowCookie(result.userId(), OtpPurpose.VERIFY_ACCOUNT).toString())
                .body(ApiResponse.success("Đăng ký thành công", result.response()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result;
        try {
            result = authService.login(request);
        } catch (UnverifiedAccountException exception) {
            ApiErrorResponse error = new ApiErrorResponse(
                    exception.getErrorCode().getCode(), exception.getMessage(), UUID.randomUUID().toString(), exception.getChannels());
            return ResponseEntity.status(exception.getErrorCode().getHttpStatus())
                    .header("Set-Cookie", flowCookie(exception.getUserId(), OtpPurpose.VERIFY_ACCOUNT).toString())
                    .body(error);
        }
        LoginResponse loginResponse = result.response();
        String accessToken = jwtService.generateAccessToken(result.userId(), loginResponse.getUsername());
        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(jwtService.accessTokenTtlSeconds()))
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString(), flowTokenService.clearCookie().toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        AuthService.ForgotPasswordResult result = authService.forgotPassword(request);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.userId() != null) {
            response.header("Set-Cookie", flowCookie(result.userId(), OtpPurpose.RESET_PASSWORD).toString());
        }
        return response.body(ApiResponse.success(
                "Nếu tài khoản tồn tại, bạn sẽ nhận được hướng dẫn", result.response()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        FlowTokenService.FlowClaims claims = flowTokenService.requireClaims(httpRequest);
        if (claims.purpose() != OtpPurpose.RESET_PASSWORD || !claims.verified()) {
            throw new com.example.otpdemo.exception.BusinessException(
                    com.example.otpdemo.exception.ErrorCode.FLOW_INVALID);
        }
        authService.resetPassword(claims.userId(), request);
        return ResponseEntity.ok()
                .header("Set-Cookie", flowTokenService.clearCookie().toString())
                .body(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    private ResponseCookie flowCookie(Long userId, OtpPurpose purpose) {
        return flowTokenService.buildCookie(flowTokenService.createToken(userId, purpose));
    }
}

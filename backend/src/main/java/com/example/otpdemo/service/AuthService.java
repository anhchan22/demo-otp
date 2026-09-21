package com.example.otpdemo.service;

import com.example.otpdemo.dto.request.RegisterRequest;
import com.example.otpdemo.dto.request.LoginRequest;
import com.example.otpdemo.dto.request.ForgotPasswordRequest;
import com.example.otpdemo.dto.request.ResetPasswordRequest;
import com.example.otpdemo.dto.response.ForgotPasswordResponse;
import com.example.otpdemo.dto.response.LoginResponse;
import com.example.otpdemo.dto.response.RecoveryChannelDto;
import com.example.otpdemo.dto.response.RegisterResponse;
import com.example.otpdemo.entity.OtpLog;
import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.enums.OtpStatus;
import com.example.otpdemo.enums.UserStatus;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import com.example.otpdemo.exception.UnverifiedAccountException;
import com.example.otpdemo.repository.UserRepository;
import com.example.otpdemo.repository.OtpLogRepository;
import com.example.otpdemo.util.MaskingUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpLogRepository otpLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResult register(RegisterRequest request) {
        String email = normalizeOptional(request.getEmail());
        String phone = normalizeOptional(request.getPhone());
        validateUniqueFields(request, email, phone);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPhone(phone);

        User savedUser = userRepository.save(user);
        return new RegisterResult(savedUser.getId(), toRegisterResponse(savedUser));
    }

    private void validateUniqueFields(RegisterRequest request, String email, String phone) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private RegisterResponse toRegisterResponse(User user) {
        return RegisterResponse.builder()
                .username(user.getUsername())
                .status(user.getStatus())
                .email(MaskingUtils.maskEmail(user.getEmail()))
                .phone(MaskingUtils.maskPhone(user.getPhone()))
                .channels(recoveryChannels(user))
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new UnverifiedAccountException(user.getId(), recoveryChannels(user));
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        LoginResponse response = LoginResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
        return new LoginResult(user.getId(), response);
    }

    private List<RecoveryChannelDto> recoveryChannels(User user) {
        List<RecoveryChannelDto> channels = new ArrayList<>();
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            channels.add(RecoveryChannelDto.builder()
                    .channel(OtpChannel.EMAIL)
                    .destination(MaskingUtils.maskEmail(user.getEmail()))
                    .build());
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            channels.add(RecoveryChannelDto.builder()
                    .channel(OtpChannel.SMS)
                    .destination(MaskingUtils.maskPhone(user.getPhone()))
                    .build());
        }
        return channels;
    }

    @Transactional(readOnly = true)
    public ForgotPasswordResult forgotPassword(ForgotPasswordRequest request) {
        String identifier = request.getIdentifier().trim();
        return userRepository.findByUsernameOrEmailOrPhone(identifier, identifier, identifier)
                .map(user -> new ForgotPasswordResult(
                        user.getId(),
                        ForgotPasswordResponse.builder().channels(recoveryChannels(user)).build()))
                .orElseGet(() -> new ForgotPasswordResult(
                        null,
                        ForgotPasswordResponse.builder().channels(List.of()).build()));
    }

    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        OtpLog otpLog = otpLogRepository
                .findTopByUserIdAndPurposeAndStatusOrderByVerifiedAtDesc(
                        userId, OtpPurpose.RESET_PASSWORD, OtpStatus.VERIFIED)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESET_TOKEN_INVALID));

        User user = otpLog.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpLog.setStatus(OtpStatus.INVALIDATED);
        otpLog.setInvalidatedAt(Instant.now());
        otpLogRepository.save(otpLog);
    }

    public record LoginResult(Long userId, LoginResponse response) {
    }

    public record RegisterResult(Long userId, RegisterResponse response) {
    }

    public record ForgotPasswordResult(Long userId, ForgotPasswordResponse response) {
    }
}

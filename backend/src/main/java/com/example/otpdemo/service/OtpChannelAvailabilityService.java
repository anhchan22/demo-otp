package com.example.otpdemo.service;

import com.example.otpdemo.dto.response.OtpChannelAvailabilityDto;
import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.enums.UserStatus;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import com.example.otpdemo.repository.UserRepository;
import com.example.otpdemo.service.sender.OtpSender;
import com.example.otpdemo.service.sender.OtpSenderRouter;
import com.example.otpdemo.util.MaskingUtils;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpChannelAvailabilityService {

    private final UserRepository userRepository;
    private final OtpSenderRouter otpSenderRouter;

    @Transactional(readOnly = true)
    public List<OtpChannelAvailabilityDto> getChannels(Long userId, OtpPurpose purpose) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return Arrays.stream(OtpChannel.values())
                .map(channel -> toDto(user, channel, purpose))
                .toList();
    }

    private OtpChannelAvailabilityDto toDto(User user, OtpChannel channel, OtpPurpose purpose) {
        OtpSender sender = otpSenderRouter.senderFor(channel);
        String destination = destination(user, channel);
        String reason = null;
        boolean hasDestination = destination != null;
        boolean available = sender != null && sender.isAvailable() && hasDestination;

        if (!hasDestination) {
            reason = "Tài khoản chưa có địa chỉ nhận OTP";
        } else if (sender == null) {
            reason = "Kênh OTP chưa được hỗ trợ";
        } else if (!sender.isAvailable()) {
            reason = sender.unavailableReason();
        }
        if (purpose == OtpPurpose.VERIFY_ACCOUNT && user.getStatus() == UserStatus.ACTIVE) {
            available = false;
            reason = "Tài khoản đã được xác thực";
        }

        return new OtpChannelAvailabilityDto(
                channel,
                available,
                sender != null && sender.isSandbox(),
                maskedDestination(user, channel),
                reason);
    }

    private String destination(User user, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL) {
            return user.getEmail();
        }
        return user.getPhone();
    }

    private String maskedDestination(User user, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL && user.getEmail() != null) {
            return MaskingUtils.maskEmail(user.getEmail());
        }
        if (channel != OtpChannel.EMAIL && user.getPhone() != null) {
            return MaskingUtils.maskPhone(user.getPhone());
        }
        return null;
    }
}

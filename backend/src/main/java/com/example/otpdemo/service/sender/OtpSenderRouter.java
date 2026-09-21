package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpSenderRouter {

    private final List<OtpSender> senders;

    public OtpSender senderFor(OtpChannel channel) {
        return senders.stream()
                .filter(sender -> sender.channel() == channel)
                .findFirst()
                .orElse(null);
    }

    public List<OtpSender> senders() {
        return senders;
    }

    public void send(User user, String rawOtp, OtpPurpose purpose, OtpChannel channel) {
        Map<OtpChannel, OtpSender> senderByChannel = new EnumMap<>(OtpChannel.class);
        senders.forEach(sender -> senderByChannel.put(sender.channel(), sender));
        OtpSender sender = senderByChannel.get(channel);
        if (sender == null) {
            throw new BusinessException(ErrorCode.OTP_INVALID_CHANNEL);
        }
        sender.send(user, rawOtp, purpose, channel);
    }
}

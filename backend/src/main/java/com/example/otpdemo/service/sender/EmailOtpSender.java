package com.example.otpdemo.service.sender;

import com.example.otpdemo.entity.User;
import com.example.otpdemo.enums.OtpChannel;
import com.example.otpdemo.enums.OtpPurpose;
import com.example.otpdemo.exception.BusinessException;
import com.example.otpdemo.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailOtpSender implements OtpSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailOtpSender(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public OtpChannel channel() {
        return OtpChannel.EMAIL;
    }

    @Override
    public boolean isAvailable() {
        return !fromAddress.isBlank();
    }

    @Override
    public String unavailableReason() {
        return "Email SMTP chưa được cấu hình";
    }

    @Override
    public void send(User user, String rawOtp, OtpPurpose purpose, OtpChannel channel) {
        if (channel != OtpChannel.EMAIL || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.OTP_INVALID_CHANNEL);
        }
        if (fromAddress.isBlank()) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject(subjectFor(purpose));
            helper.setText(bodyFor(user, rawOtp));
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String subjectFor(OtpPurpose purpose) {
        return purpose == OtpPurpose.VERIFY_ACCOUNT
                ? "Mã OTP xác thực tài khoản"
                : "Mã OTP đặt lại mật khẩu";
    }

    private String bodyFor(User user, String rawOtp) {
        String name = user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername()
                : user.getFullName();
        return "Xin chào " + name + ",\n\n"
                + "Mã OTP của bạn là:\n\n"
                + rawOtp + "\n\n"
                + "Mã có hiệu lực trong 5 phút.\n\n"
                + "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.\n\n"
                + "OTP Demo Team";
    }
}

package com.example.otpdemo.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR("VAL-4000", HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    USERNAME_ALREADY_EXISTS("USR-4091", HttpStatus.CONFLICT, "Username đã tồn tại"),
    EMAIL_ALREADY_EXISTS("USR-4092", HttpStatus.CONFLICT, "Email đã tồn tại"),
    PHONE_ALREADY_EXISTS("USR-4093", HttpStatus.CONFLICT, "Số điện thoại đã tồn tại"),
    USER_NOT_FOUND("USR-4041", HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    ACCOUNT_ALREADY_VERIFIED("USR-4001", HttpStatus.BAD_REQUEST, "Tài khoản đã được xác thực"),
    ACCOUNT_NOT_VERIFIED("USR-4031", HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực"),
    ACCOUNT_LOCKED("USR-4032", HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    INVALID_CREDENTIALS("AUTH-4011", HttpStatus.UNAUTHORIZED, "Username hoặc mật khẩu không chính xác"),
    RESET_TOKEN_INVALID("AUTH-4013", HttpStatus.UNAUTHORIZED, "Reset token không hợp lệ"),
    RESET_TOKEN_EXPIRED("AUTH-4014", HttpStatus.UNAUTHORIZED, "Reset token đã hết hạn"),
    PASSWORD_CONFIRMATION_MISMATCH("VAL-4005", HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp"),
    PASSWORD_REUSE("VAL-4006", HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng mật khẩu hiện tại"),
    OTP_INVALID("OTP-4001", HttpStatus.BAD_REQUEST, "Mã OTP không chính xác"),
    OTP_EXPIRED("OTP-4002", HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn"),
    OTP_ALREADY_USED("OTP-4003", HttpStatus.BAD_REQUEST, "Mã OTP đã được sử dụng"),
    OTP_INVALIDATED("OTP-4004", HttpStatus.BAD_REQUEST, "Mã OTP đã bị vô hiệu hóa"),
    OTP_BLOCKED("OTP-4005", HttpStatus.BAD_REQUEST, "Mã OTP đã bị khóa do nhập sai quá nhiều lần"),
    OTP_INVALID_PURPOSE("OTP-4006", HttpStatus.BAD_REQUEST, "Mục đích sử dụng OTP không hợp lệ"),
    OTP_INVALID_CHANNEL("OTP-4007", HttpStatus.BAD_REQUEST, "Kênh gửi OTP không hợp lệ"),
    OTP_NOT_FOUND("OTP-4041", HttpStatus.NOT_FOUND, "Không tìm thấy OTP"),
    OTP_RESEND_COOLDOWN("OTP-4291", HttpStatus.TOO_MANY_REQUESTS, "Vui lòng chờ trước khi gửi lại OTP"),
    OTP_TOO_MANY_REQUESTS("OTP-4292", HttpStatus.TOO_MANY_REQUESTS, "Bạn đã yêu cầu OTP quá nhiều lần"),
    OTP_OWNERSHIP("OTP-4031", HttpStatus.FORBIDDEN, "Không có quyền truy cập OTP này"),
    FLOW_EXPIRED("FLOW-4011", HttpStatus.UNAUTHORIZED, "Phiên xác thực đã hết hạn. Vui lòng thử lại."),
    FLOW_INVALID("FLOW-4012", HttpStatus.UNAUTHORIZED, "Phiên xác thực không hợp lệ."),
    EMAIL_SEND_FAILED("SYS-5031", HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi email OTP"),
    SMS_SEND_FAILED("SYS-5032", HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi SMS OTP"),
    SMS_PROVIDER_NOT_CONFIGURED("SYS-5033", HttpStatus.SERVICE_UNAVAILABLE, "SMS provider chưa được cấu hình"),
    ZALO_PROVIDER_NOT_CONFIGURED("SYS-5034", HttpStatus.SERVICE_UNAVAILABLE, "Zalo provider chưa được cấu hình"),
    OTP_SECRET_NOT_CONFIGURED("SYS-5000", HttpStatus.INTERNAL_SERVER_ERROR, "OTP secret chưa được cấu hình"),
    SYSTEM_ERROR("SYS-5000", HttpStatus.INTERNAL_SERVER_ERROR, "Có lỗi xảy ra, vui lòng thử lại");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

}

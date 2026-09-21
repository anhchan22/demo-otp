package com.example.otpdemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 128, message = "Mật khẩu mới phải có từ 8 đến 128 ký tự")
    private String newPassword;

    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    @Size(max = 128, message = "Mật khẩu xác nhận không được vượt quá 128 ký tự")
    private String confirmPassword;
}

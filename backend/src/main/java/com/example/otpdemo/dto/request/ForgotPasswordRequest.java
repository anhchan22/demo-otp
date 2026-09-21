package com.example.otpdemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ForgotPasswordRequest {
    @NotBlank(message = "Username, email hoặc số điện thoại không được để trống")
    @Size(max = 255, message = "Thông tin tài khoản không được vượt quá 255 ký tự")
    private String identifier;
}

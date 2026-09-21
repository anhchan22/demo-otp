package com.example.otpdemo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String username;
    private String fullName;
}

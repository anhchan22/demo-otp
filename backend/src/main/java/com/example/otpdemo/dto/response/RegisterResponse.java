package com.example.otpdemo.dto.response;

import com.example.otpdemo.enums.UserStatus;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private String username;
    private UserStatus status;
    private String email;
    private String phone;
    private List<RecoveryChannelDto> channels;
}

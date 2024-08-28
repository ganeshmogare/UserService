package com.scaler.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {
    private String token;
    private String ipAddress;
    private String deviceInfo;
}

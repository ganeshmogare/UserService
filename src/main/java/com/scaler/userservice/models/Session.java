package com.scaler.userservice.models;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Session extends BaseModel{
    private String token;
    private LocalDateTime expiry;
    private String ipAddress;
    private String deviceInfo;
}

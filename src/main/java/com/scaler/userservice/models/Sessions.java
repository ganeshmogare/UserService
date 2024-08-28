package com.scaler.userservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.support.SessionStatus;

import java.sql.Date;

@Entity
@Getter
@Setter
public class Sessions extends BaseModel{
    private String token;
    private Date expiringAt;
    @ManyToOne
    private User user;
    @Enumerated(EnumType.ORDINAL)
    private SessionStatus sessionStatus;
}

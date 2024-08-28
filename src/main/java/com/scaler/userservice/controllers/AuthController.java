package com.scaler.userservice.controllers;

import com.scaler.userservice.dtos.SignUpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController<SingUpResponse> {

    @PostMapping("/sign_up")
    public SingUpResponse signUp(SignUpRequest request) {
        return null;
    }

    @PostMapping("/login")
    public String login() {
        return null;
    }

}

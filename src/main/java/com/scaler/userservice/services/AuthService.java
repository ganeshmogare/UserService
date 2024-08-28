package com.scaler.userservice.services;

import org.springframework.stereotype.Service;

@Service("authService")
public class AuthService {
    public void signup() {
        System.out.println("Signup");
    }

    public void login() {
        System.out.println("Login");
    }

    public void logout() {
        System.out.println("Logout");
    }

    public void updateRole() {
        System.out.println("Update Role");
    }
}

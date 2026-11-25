package com.employeemanagement.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.AuthDtos.ChangePasswordRequest;
import com.employeemanagement.dto.AuthDtos.LoginRequest;
import com.employeemanagement.dto.AuthDtos.LoginResponse;
import com.employeemanagement.service.AuthService;

import jakarta.validation.Valid;

/**
 * Sign-in is stateless: the client keeps the token and signs out by discarding it.
 * Changing the password invalidates every other token for the account.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/change-password")
    public LoginResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return service.changePassword(request);
    }
}

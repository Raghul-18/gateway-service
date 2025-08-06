package com.bank.gateway.controller;

import com.bank.gateway.dto.*;
import com.bank.gateway.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        String sessionId = authService.sendOtp(request);
        return ResponseEntity.ok().body(
                // Optional: You can return sessionId only in dev, or hide in prod
                java.util.Map.of("success", true, "message", "OTP sent", "sessionId", sessionId)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<JwtResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<JwtResponse> loginAdmin(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(authService.loginAdmin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}

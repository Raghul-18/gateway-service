package com.bank.gateway.controller;

import com.bank.gateway.dto.*;
import com.bank.gateway.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
    public ResponseEntity<JwtResponse> loginAdmin(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse response) {

        JwtResponse jwtResponse = authService.loginAdmin(request);
        String jwtToken = jwtResponse.getToken();

        // Set JWT as HttpOnly Secure cookie with SameSite attribute
        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);         // set true in production with HTTPS
        cookie.setPath("/");             // cookie valid for entire app
        cookie.setMaxAge(60 * 60 * 24);  // 1 day expiry
        
        // Manually build Set-Cookie header with SameSite attribute
        String cookieHeader = String.format(
            "%s=%s; Path=%s; HttpOnly; SameSite=Strict; Max-Age=%d%s",
            cookie.getName(),
            cookie.getValue(),
            cookie.getPath(),
            cookie.getMaxAge(),
            cookie.getSecure() ? "; Secure" : ""
        );
        
        response.addHeader("Set-Cookie", cookieHeader);

        // Return original response as JSON
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}

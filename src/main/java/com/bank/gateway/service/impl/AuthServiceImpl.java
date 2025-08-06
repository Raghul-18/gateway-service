package com.bank.gateway.service.impl;

import com.bank.gateway.dto.*;
import com.bank.gateway.entity.User;
import com.bank.gateway.exception.CustomAuthException;
import com.bank.gateway.repository.UserRepository;
import com.bank.gateway.security.JwtUtils;
import com.bank.gateway.service.AuthService;
import com.bank.gateway.service.SmsOtpService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SmsOtpService smsOtpService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String sendOtp(OtpRequest request) {
        return smsOtpService.sendOtp(request.getPhone());
    }

    @Override
    public JwtResponse verifyOtp(OtpVerifyRequest request) {
        boolean valid = smsOtpService.verifyOtp(request.getSessionId(), request.getOtp());
        if (!valid) {
            throw new CustomAuthException("Invalid OTP");
        }

        String phone = request.getPhone().replace("+91", "");
        String username = "customer_" + phone;

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username(username)
                                .role("CUSTOMER")
                                .enabled(true)
                                .build()
                ));

        return JwtResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .token(jwtUtils.generateToken(user))
                .build();
    }

    @Override
    public JwtResponse loginAdmin(AdminLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomAuthException("Invalid username or password"));

        System.out.println(">> [DEBUG] Input Password: " + request.getPassword());
        System.out.println(">> [DEBUG] Stored Hash: " + user.getPasswordHash());

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        System.out.println(">> [DEBUG] Matches: " + matches);

        if (!"ADMIN".equalsIgnoreCase(user.getRole()) || !user.isEnabled()) {
            throw new CustomAuthException("Access denied");
        }

        if (!matches) {
            throw new CustomAuthException("Invalid username or password");
        }

        return JwtResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .token(jwtUtils.generateToken(user))
                .build();
    }


    @Override
    public JwtResponse refreshToken(String oldToken) {
        if (!jwtUtils.validateToken(oldToken)) {
            throw new CustomAuthException("Invalid token");
        }

        User user = jwtUtils.extractUserFromToken(oldToken);

        return JwtResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .token(jwtUtils.generateToken(user))
                .build();
    }
}

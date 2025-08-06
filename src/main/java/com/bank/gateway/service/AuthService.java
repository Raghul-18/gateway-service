package com.bank.gateway.service;

import com.bank.gateway.dto.*;

public interface AuthService {

    String sendOtp(OtpRequest request);

    JwtResponse verifyOtp(OtpVerifyRequest request);

    JwtResponse loginAdmin(AdminLoginRequest request);

    JwtResponse refreshToken(String oldToken);
}

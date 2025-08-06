package com.bank.gateway.service;

public interface SmsOtpService {

    /**
     * Sends an OTP via SMS to the specified phone number.
     * @param phone full phone number (e.g. +919876543210)
     * @return session ID from 2Factor
     */
    String sendOtp(String phone);

    /**
     * Verifies the OTP using session ID and code.
     * @param sessionId session ID received from 2Factor
     * @param otp OTP entered by the user
     * @return true if OTP is valid, false otherwise
     */
    boolean verifyOtp(String sessionId, String otp);
}

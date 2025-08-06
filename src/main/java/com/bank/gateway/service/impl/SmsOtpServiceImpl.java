package com.bank.gateway.service.impl;

import com.bank.gateway.service.SmsOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsOtpServiceImpl implements SmsOtpService {

    @Value("${otp.api-key}")
    private String apiKey;

    @Value("${otp.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String sendOtp(String phone) {
        String url = String.format("%s/%s/SMS/%s/AUTOGEN", baseUrl, apiKey, phone);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.info("OTP sent to {} | session_id: {}", phone, response.getBody().get("Details"));
            return response.getBody().get("Details").toString();
        }
        throw new RuntimeException("Failed to send OTP");
    }

    @Override
    public boolean verifyOtp(String sessionId, String otp) {
        String url = String.format("%s/%s/SMS/VERIFY/%s/%s", baseUrl, apiKey, sessionId, otp);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String status = response.getBody().get("Status").toString();
            return "Success".equalsIgnoreCase(status);
        }
        return false;
    }
}

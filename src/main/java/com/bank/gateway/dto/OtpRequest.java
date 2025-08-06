package com.bank.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {

    @NotBlank(message = "Phone number is required")
    private String phone; // E.g., +919876543210
}

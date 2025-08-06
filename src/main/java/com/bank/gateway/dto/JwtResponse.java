package com.bank.gateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {

    private String token;
    private Long userId;
    private String role;
}

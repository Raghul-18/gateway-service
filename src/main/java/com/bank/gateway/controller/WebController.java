package com.bank.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * Serves the main banking application HTML page
     */
    @GetMapping("/")
    public String index() {
        return "complete_banking_system";
    }

    /**
     * Serves the banking app on multiple routes for SPA-like behavior
     */
    @GetMapping({"/login", "/dashboard", "/admin", "/registration", "/kyc"})
    public String app() {
        return "complete_banking_system";
    }
}
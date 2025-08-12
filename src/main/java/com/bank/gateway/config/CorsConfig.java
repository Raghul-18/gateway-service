// File: com.bank.gateway.config.CorsConfig.java
package com.bank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Allow all paths
                        .allowedOrigins("http://localhost:8084") // UI origin
                        .allowedOrigins("http://localhost:8081")
                        .allowedOrigins("http://localhost:8082")
                        .allowedOrigins("http://localhost:8083")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true); // Needed if sending cookies or Authorization headers
            }
        };
    }
}

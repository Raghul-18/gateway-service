package com.bank.gateway.exception;

public class CustomAuthException extends RuntimeException {
    public CustomAuthException(String message) {
        super(message);
    }
}

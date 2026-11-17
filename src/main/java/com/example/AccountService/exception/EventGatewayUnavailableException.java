package com.example.AccountService.exception;

public class EventGatewayUnavailableException extends RuntimeException {
    public EventGatewayUnavailableException(String message) {
        super(message);
    }
}

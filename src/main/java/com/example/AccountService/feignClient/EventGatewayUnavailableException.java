package com.example.AccountService.feignClient;

public class EventGatewayUnavailableException extends RuntimeException {
    public EventGatewayUnavailableException(String message) {
        super(message);
    }
}

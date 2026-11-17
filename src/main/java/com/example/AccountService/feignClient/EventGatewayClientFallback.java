 package com.example.AccountService.feignClient;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventGatewayClientFallback implements EventGatewayClient {

    @Override
    public EventResponse getEventById(String eventId) {
        throw new EventGatewayUnavailableException("Event Gateway is unavailable while retrieving event " + eventId);
    }

    @Override
    public List<EventResponse> getEventsByAccount(String accountId) {
        throw new EventGatewayUnavailableException(
                "Event Gateway is unavailable while retrieving events for account " + accountId);
    }
}
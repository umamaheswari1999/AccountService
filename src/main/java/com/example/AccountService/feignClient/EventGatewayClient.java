package com.example.AccountService.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;

@FeignClient(
        name = "event-gateway",
        url = "${event-gateway.url}",
        fallback = EventGatewayClientFallback.class
)
public interface EventGatewayClient {

    @GetMapping("/events/{id}")
    EventResponse getEventById(@PathVariable("id") String eventId);

    @GetMapping("/events")
    List<EventResponse> getEventsByAccount(@RequestParam("account") String accountId);
}

// DTO matching the Gateway's event structure
record EventResponse(
        String eventId,
        String accountId,
        String type,
        double amount,
        String currency,
        Instant eventTimestamp,
        Object metadata
) {}
package com.example.AccountService.feignClient;

import com.example.AccountService.exception.EventGatewayUnavailableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventGatewayClientFallbackTests {
    private final EventGatewayClientFallback fallback = new EventGatewayClientFallback();

    @Test
    void throwsForUnavailableSingleEventLookup() {
        RuntimeException exception = assertThrows(
                EventGatewayUnavailableException.class, () -> fallback.getEventById("event-1"));

        assertTrue(exception.getMessage().contains("event-1"));
    }

    @Test
    void throwsForUnavailableAccountLookup() {
        RuntimeException exception = assertThrows(
                EventGatewayUnavailableException.class, () -> fallback.getEventsByAccount("account-1"));

        assertTrue(exception.getMessage().contains("account-1"));
    }

    @Test
    void eventResponseExposesItsValues() {
        EventResponse response = new EventResponse(
                "event-2", "account-2", "CREDIT", 2.5, "USD", java.time.Instant.EPOCH, null);

        assertEquals("event-2", response.eventId());
        assertEquals("account-2", response.accountId());
        assertEquals("CREDIT", response.type());
        assertEquals(2.5, response.amount());
        assertEquals("USD", response.currency());
        assertEquals(java.time.Instant.EPOCH, response.eventTimestamp());
        assertNull(response.metadata());
        assertNotNull(response.toString());
    }
}

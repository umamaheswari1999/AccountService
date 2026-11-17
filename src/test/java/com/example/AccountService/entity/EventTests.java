package com.example.AccountService.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventTests {
    @Test
    void constructorsBuilderAndAccessorsPreserveEventData() {
        Account account = new Account("account-1");
        Instant timestamp = Instant.parse("2025-05-06T07:08:09Z");
        Event empty = new Event();
        assertNull(empty.getEventId());

        Event event = new Event("event-1", new BigDecimal("12.34"), "CREDIT", timestamp, account);
        assertEquals("event-1", event.getEventId());
        assertEquals(new BigDecimal("12.34"), event.getAmount());
        assertEquals("CREDIT", event.getType());
        assertEquals(timestamp, event.getEventTimestamp());
        assertSame(account, event.getAccount());

        Event built = Event.builder()
                .id(10L)
                .eventId("event-2")
                .amount(BigDecimal.ONE)
                .type("DEBIT")
                .eventTimestamp(timestamp)
                .account(account)
                .build();
        built.setId(11L);
        assertEquals(11L, built.getId());
        assertNotNull(built.toString());
    }
}

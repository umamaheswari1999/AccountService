package com.example.AccountService.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTests {
    @Test
    void recordDtosExposeValuesAndValueSemantics() {
        Instant timestamp = Instant.parse("2025-06-07T08:09:10Z");
        BalanceResponse balance = new BalanceResponse("account-1", new BigDecimal("1.00"));
        AccountTransactionDto transaction =
                new AccountTransactionDto("event-1", BigDecimal.ONE, "CREDIT", timestamp);
        AccountDetailsDto details = new AccountDetailsDto("account-1", BigDecimal.ONE, List.of(transaction));
        TransactionRequest request = new TransactionRequest("event-1", BigDecimal.ONE, "CREDIT", timestamp);

        assertEquals("account-1", balance.accountId());
        assertEquals(new BigDecimal("1.00"), balance.balance());
        assertEquals("event-1", transaction.eventId());
        assertEquals(BigDecimal.ONE, transaction.amount());
        assertEquals("CREDIT", transaction.type());
        assertEquals(timestamp, transaction.eventTimestamp());
        assertEquals(List.of(transaction), details.transactions());
        assertEquals("account-1", details.accountId());
        assertEquals(BigDecimal.ONE, details.balance());
        assertEquals("event-1", request.eventId());
        assertEquals(BigDecimal.ONE, request.amount());
        assertEquals("CREDIT", request.type());
        assertEquals(timestamp, request.eventTimestamp());
        assertEquals(balance, new BalanceResponse("account-1", new BigDecimal("1.00")));
        assertEquals(balance.hashCode(), new BalanceResponse("account-1", new BigDecimal("1.00")).hashCode());
        assertNotNull(balance.toString());
        assertNotNull(transaction.toString());
        assertNotNull(details.toString());
        assertNotNull(request.toString());
    }
}

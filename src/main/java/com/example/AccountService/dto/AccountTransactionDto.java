package com.example.AccountService.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountTransactionDto(String eventId, BigDecimal amount, String type, Instant eventTimestamp) {
}

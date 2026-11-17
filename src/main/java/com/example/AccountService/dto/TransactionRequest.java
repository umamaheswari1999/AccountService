package com.example.AccountService.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
        @NotNull String eventId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull String type,
        @NotNull Instant eventTimestamp
) {
}

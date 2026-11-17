package com.example.AccountService.dto;

import java.math.BigDecimal;

public record BalanceResponse(String accountId, BigDecimal balance) {
}

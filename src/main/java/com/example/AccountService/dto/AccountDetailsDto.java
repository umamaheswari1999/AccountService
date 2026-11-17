package com.example.AccountService.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountDetailsDto(String accountId, BigDecimal balance, List<AccountTransactionDto> transactions) {}
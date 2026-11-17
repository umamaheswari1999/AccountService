package com.example.AccountService.controller;

import com.example.AccountService.dto.AccountDetailsDto;
import com.example.AccountService.dto.BalanceResponse;
import com.example.AccountService.dto.TransactionRequest;
import com.example.AccountService.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{accountId}/transactions")
    public ResponseEntity<Void> applyTransaction(
            @PathVariable String accountId,
            @Valid @RequestBody TransactionRequest request) {
        
        accountService.applyTransaction(
                accountId,
                request.eventId(),
                request.amount(),
                request.type(),
                request.eventTimestamp()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        BigDecimal balance = accountService.getBalance(accountId);
        return ResponseEntity.ok(new BalanceResponse(accountId, balance));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailsDto> getAccountDetails(@PathVariable String accountId) {
        return accountService.getAccountDetails(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


package com.example.AccountService.service;


import com.example.AccountService.dto.AccountDetailsDto;
import com.example.AccountService.dto.AccountTransactionDto;
import com.example.AccountService.entity.Account;
import com.example.AccountService.entity.Event;
import com.example.AccountService.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account applyTransaction(String accountId, String eventId, BigDecimal amount, String type, Instant eventTimestamp) {
        Account account = accountRepository.findById(accountId)
                .orElseGet(() -> accountRepository.save(new Account(accountId)));

        // Check if event already applied (idempotency safety check)
        boolean exists = account.getTransactions().stream()
                .anyMatch(tx -> tx.getEventId().equals(eventId));
        if (exists) {
            return account;
        }
        log.info("Applying transaction for accountId: {}, eventId: {}, amount: {}, type: {}, eventTimestamp: {}",accountId, eventId, amount, type, eventTimestamp);

        account.applyTransaction(eventId, amount, type, eventTimestamp);
        return accountRepository.save(account);
    }

    public BigDecimal getBalance(String accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public Optional<AccountDetailsDto> getAccountDetails(String accountId) {
        return accountRepository.findById(accountId).map(acc -> {
            // Sort transactions chronologically by eventTimestamp (out-of-order tolerance requirement)
            List<AccountTransactionDto> sortedTx = acc.getTransactions().stream()
                    .sorted(Comparator.comparing(Event::getEventTimestamp))
                    .map(tx -> new AccountTransactionDto(
                            tx.getEventId(),
                            tx.getAmount(),
                            tx.getType(),
                            tx.getEventTimestamp()
                    ))
                    .toList();

            return new AccountDetailsDto(acc.getAccountId(), acc.getBalance(), sortedTx);
        });
    }
}


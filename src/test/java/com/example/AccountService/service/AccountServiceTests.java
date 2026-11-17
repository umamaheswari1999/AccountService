package com.example.AccountService.service;

import com.example.AccountService.dto.AccountDetailsDto;
import com.example.AccountService.entity.Account;
import com.example.AccountService.repo.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTests {
    private AccountRepository accountRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void createsAccountAndAppliesCredit() {
        Instant timestamp = Instant.parse("2025-01-02T03:04:05Z");
        when(accountRepository.findById("account-1")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.applyTransaction(
                "account-1", "event-1", new BigDecimal("25.50"), "CREDIT", timestamp);

        assertEquals("account-1", account.getAccountId());
        assertEquals(new BigDecimal("25.50"), account.getBalance());
        assertEquals(1, account.getTransactions().size());
        assertEquals("event-1", account.getTransactions().get(0).getEventId());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void appliesDebitToExistingAccount() {
        Account account = new Account("account-2");
        account.applyTransaction("earlier", new BigDecimal("100.00"), "CREDIT", Instant.EPOCH);
        when(accountRepository.findById("account-2")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.applyTransaction(
                "account-2", "event-2", new BigDecimal("30.00"), "DEBIT", Instant.EPOCH.plusSeconds(1));

        assertSame(account, result);
        assertEquals(new BigDecimal("70.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void skipsAlreadyAppliedEvent() {
        Account account = new Account("account-3");
        account.applyTransaction("duplicate", new BigDecimal("12.00"), "CREDIT", Instant.EPOCH);
        when(accountRepository.findById("account-3")).thenReturn(Optional.of(account));

        Account result = accountService.applyTransaction(
                "account-3", "duplicate", new BigDecimal("12.00"), "CREDIT", Instant.EPOCH);

        assertSame(account, result);
        assertEquals(new BigDecimal("12.00"), account.getBalance());
        assertEquals(1, account.getTransactions().size());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void returnsExistingBalanceAndZeroForMissingAccount() {
        Account account = new Account("account-4");
        account.setBalance(new BigDecimal("9.25"));
        when(accountRepository.findById("account-4")).thenReturn(Optional.of(account));
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        assertEquals(new BigDecimal("9.25"), accountService.getBalance("account-4"));
        assertEquals(BigDecimal.ZERO, accountService.getBalance("missing"));
    }

    @Test
    void returnsDetailsWithTransactionsInTimestampOrder() {
        Instant later = Instant.parse("2025-02-02T00:00:00Z");
        Instant earlier = Instant.parse("2025-01-01T00:00:00Z");
        Account account = new Account("account-5");
        account.applyTransaction("later-event", new BigDecimal("2.00"), "CREDIT", later);
        account.applyTransaction("earlier-event", new BigDecimal("1.00"), "DEBIT", earlier);
        when(accountRepository.findById("account-5")).thenReturn(Optional.of(account));

        AccountDetailsDto details = accountService.getAccountDetails("account-5").orElseThrow();

        assertEquals("account-5", details.accountId());
        assertEquals("earlier-event", details.transactions().get(0).eventId());
        assertEquals("later-event", details.transactions().get(1).eventId());
        assertEquals(new BigDecimal("1.00"), details.transactions().get(0).amount());
    }

    @Test
    void returnsEmptyDetailsForMissingAccount() {
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        assertTrue(accountService.getAccountDetails("missing").isEmpty());
    }
}

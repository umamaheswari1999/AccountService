package com.example.AccountService.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AccountTests {
    @Test
    void constructorsAndBuilderInitializeAndExposeFields() {
        Account empty = new Account();
        assertEquals(BigDecimal.ZERO, empty.getBalance());
        assertNotNull(empty.getTransactions());
        assertEquals("account-1", new Account("account-1").getAccountId());

        Account account = Account.builder()
                .accountId("account-2")
                .balance(new BigDecimal("7.50"))
                .transactions(new ArrayList<>())
                .build();
        account.setAccountId("account-3");
        account.setBalance(new BigDecimal("8.50"));

        assertEquals("account-3", account.getAccountId());
        assertEquals(new BigDecimal("8.50"), account.getBalance());
        assertNotNull(account.toString());
    }

    @Test
    void appliesCreditDebitAndUnknownTransactionTypes() {
        Account account = new Account("account-4");
        Instant timestamp = Instant.parse("2025-04-05T06:07:08Z");

        account.applyTransaction("credit", new BigDecimal("10.00"), "credit", timestamp);
        account.applyTransaction("debit", new BigDecimal("3.00"), "DEBIT", timestamp.plusSeconds(1));
        account.applyTransaction("unknown", new BigDecimal("99.00"), "OTHER", timestamp.plusSeconds(2));

        assertEquals(new BigDecimal("7.00"), account.getBalance());
        assertEquals(3, account.getTransactions().size());
        assertSame(account, account.getTransactions().get(0).getAccount());
        assertEquals("unknown", account.getTransactions().get(2).getEventId());
    }
}

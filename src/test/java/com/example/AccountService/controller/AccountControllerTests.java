package com.example.AccountService.controller;

import com.example.AccountService.dto.AccountDetailsDto;
import com.example.AccountService.dto.AccountTransactionDto;
import com.example.AccountService.dto.BalanceResponse;
import com.example.AccountService.dto.TransactionRequest;
import com.example.AccountService.service.AccountService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerTests {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();
    private AccountService accountService;
    private AccountController controller;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        controller = new AccountController(accountService);
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void acceptsValidTransactionRequestAndReturnsOk() {
        Instant timestamp = Instant.parse("2025-03-04T05:06:07Z");
        TransactionRequest request = new TransactionRequest("event-1", new BigDecimal("0.01"), "CREDIT", timestamp);

        ResponseEntity<Void> response = controller.applyTransaction("account-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService).applyTransaction(
                "account-1", "event-1", new BigDecimal("0.01"), "CREDIT", timestamp);
    }

    @Test
    void rejectsNullFieldsAndAmountsBelowMinimum() {
        TransactionRequest invalid = new TransactionRequest(null, BigDecimal.ZERO, null, null);

        assertEquals(4, VALIDATOR.validate(invalid).size());
        assertEquals(0, VALIDATOR.validate(
                new TransactionRequest("event", new BigDecimal("0.01"), "DEBIT", Instant.EPOCH)).size());
    }

    @Test
    void returnsBalanceResponse() {
        when(accountService.getBalance("account-2")).thenReturn(new BigDecimal("45.67"));

        ResponseEntity<BalanceResponse> response = controller.getBalance("account-2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new BalanceResponse("account-2", new BigDecimal("45.67")), response.getBody());
    }

    @Test
    void returnsAccountDetailsWhenPresent() {
        AccountDetailsDto details = new AccountDetailsDto(
                "account-3", new BigDecimal("4.00"),
                List.of(new AccountTransactionDto("event-3", new BigDecimal("4.00"), "CREDIT", Instant.EPOCH)));
        when(accountService.getAccountDetails("account-3")).thenReturn(Optional.of(details));

        ResponseEntity<AccountDetailsDto> response = controller.getAccountDetails("account-3");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(details, response.getBody());
    }

    @Test
    void returnsNotFoundWhenDetailsAreMissing() {
        when(accountService.getAccountDetails("missing")).thenReturn(Optional.empty());

        ResponseEntity<AccountDetailsDto> response = controller.getAccountDetails("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}

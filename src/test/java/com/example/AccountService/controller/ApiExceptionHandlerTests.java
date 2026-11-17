package com.example.AccountService.controller;

import com.example.AccountService.dto.ApiErrorResponse;
import com.example.AccountService.exception.ApiExceptionHandler;
import com.example.AccountService.feignClient.EventGatewayUnavailableException;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTests {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsGatewayUnavailableTo503() {
        assertUnavailable(handler.handleUnavailableService(new EventGatewayUnavailableException("offline")));
    }

    @Test
    void mapsOpenCircuitBreakerTo503() {
        assertUnavailable(handler.handleUnavailableService(
                CallNotPermittedException.createCallNotPermittedException(
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("event-gateway"))));
    }

    @Test
    void mapsFullBulkheadTo503() {
        assertUnavailable(handler.handleUnavailableService(
                BulkheadFullException.createBulkheadFullException(Bulkhead.ofDefaults("event-gateway"))));
    }

    @Test
    void mapsDatabaseConnectivityFailuresTo503() {
        assertUnavailable(handler.handleUnavailableService(new DataAccessResourceFailureException("offline")));
        assertUnavailable(handler.handleUnavailableService(new SQLException("offline")));
    }

    @Test
    void mapsDataIntegrityFailuresToConflict() {
        assertError(handler.handleDataIntegrityViolation(new DataIntegrityViolationException("duplicate")),
                HttpStatus.CONFLICT);
    }

    @Test
    void mapsOtherDatabaseFailuresToInternalServerError() {
        DataAccessException exception = new DataAccessException("failed") {};

        assertError(handler.handleDataAccessException(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void mapsValidationFailuresToBadRequest() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.reject("invalid");
        MethodArgumentNotValidException invalidArgument = new MethodArgumentNotValidException(null, bindingResult);

        assertError(handler.handleValidationException(invalidArgument), HttpStatus.BAD_REQUEST);
        assertError(handler.handleValidationException(new ConstraintViolationException("invalid", Set.of())),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void mapsMalformedAndInvalidParametersToBadRequest() {
        assertError(handler.handleInvalidRequest(new HttpMessageNotReadableException("malformed", null)),
                HttpStatus.BAD_REQUEST);
        assertError(handler.handleInvalidRequest(
                new MissingServletRequestParameterException("account", "String")), HttpStatus.BAD_REQUEST);
        assertError(handler.handleInvalidRequest(new MethodArgumentTypeMismatchException(
                "wrong", Integer.class, "accountId", null, new IllegalArgumentException())),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void preservesHttpErrorStatusWithoutExposingExceptionDetails() {
        ErrorResponseException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "private detail");
        ResponseEntity<ApiErrorResponse> response = handler.handleHttpErrorResponse(exception);

        assertError(response, HttpStatus.NOT_FOUND);
        assertEquals("Not Found.", response.getBody().message());
    }

    @Test
    void mapsUnexpectedExceptionsToInternalServerErrorWithoutLeakingDetails() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpectedException(new IllegalStateException("private detail"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals("An unexpected error occurred.", response.getBody().message());
    }

    @Test
    void returnsStructuredErrorBody() {
        ApiErrorResponse body = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate")).getBody();

        assertNotNull(body);
        assertEquals(409, body.status());
        assertEquals("Conflict", body.error());
        assertNotNull(body.message());
        assertEquals(body, new ApiErrorResponse(body.status(), body.error(), body.message()));
        assertEquals(body.hashCode(), new ApiErrorResponse(body.status(), body.error(), body.message()).hashCode());
        assertNotNull(body.toString());
    }

    private void assertUnavailable(ResponseEntity<ApiErrorResponse> response) {
        assertError(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void assertError(ResponseEntity<ApiErrorResponse> response, HttpStatus status) {
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(status.value(), response.getBody().status());
        assertEquals(status.getReasonPhrase(), response.getBody().error());
    }
}

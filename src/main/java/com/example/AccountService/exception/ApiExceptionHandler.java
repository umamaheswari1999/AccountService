package com.example.AccountService.exception;

import com.example.AccountService.dto.ApiErrorResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            EventGatewayUnavailableException.class,
            CallNotPermittedException.class,
            BulkheadFullException.class,
            DataAccessResourceFailureException.class,
            SQLException.class
    })
    public ResponseEntity<ApiErrorResponse> handleUnavailableService(Exception exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "A required service or database is unavailable.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return error(HttpStatus.CONFLICT, "The request conflicts with existing data.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessException(DataAccessException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "The database operation could not be completed.");
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiErrorResponse> handleValidationException(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed.");
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "The request is malformed or contains an invalid parameter.");
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpErrorResponse(ErrorResponseException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return error(status, status.getReasonPhrase() + ".");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
        ApiErrorResponse body = new ApiErrorResponse(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(body);
    }
}

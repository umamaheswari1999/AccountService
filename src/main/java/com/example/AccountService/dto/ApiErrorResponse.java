package com.example.AccountService.dto;

public record ApiErrorResponse(int status, String error, String message) {
}

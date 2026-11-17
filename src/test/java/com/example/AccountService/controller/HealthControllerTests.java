package com.example.AccountService.controller;

import com.example.AccountService.dto.HealthResponse;
import com.example.AccountService.feignClient.HealthStatusProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthControllerTests {
    private HealthStatusProvider healthStatusProvider;
    private HealthController controller;

    @BeforeEach
    void setUp() {
        healthStatusProvider = mock(HealthStatusProvider.class);
        controller = new HealthController(healthStatusProvider);
    }

    @Test
    void returnsOkWhenApplicationIsHealthy() {
        when(healthStatusProvider.getStatus()).thenReturn("UP");

        ResponseEntity<HealthResponse> response = controller.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new HealthResponse("UP"), response.getBody());
    }

    @Test
    void returnsServiceUnavailableWhenApplicationIsNotHealthy() {
        when(healthStatusProvider.getStatus()).thenReturn("DOWN");

        ResponseEntity<HealthResponse> response = controller.health();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(new HealthResponse("DOWN"), response.getBody());
    }
}

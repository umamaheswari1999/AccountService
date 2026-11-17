package com.example.AccountService.controller;

import com.example.AccountService.dto.HealthResponse;
import com.example.AccountService.feignClient.HealthStatusProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final HealthStatusProvider healthStatusProvider;

    public HealthController(HealthStatusProvider healthStatusProvider) {
        this.healthStatusProvider = healthStatusProvider;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        String status = healthStatusProvider.getStatus();
        HealthResponse response = new HealthResponse(status);

        if ("UP".equals(status)) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

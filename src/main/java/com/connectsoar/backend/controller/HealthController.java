package com.connectsoar.backend.controller;

import com.connectsoar.backend.security.PublicEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @PublicEndpoint
    @GetMapping(value = {"/", "/health", "/api/v1/health"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ConnectSoar Backend API");
        response.put("timestamp", Instant.now().toString());
        response.put("version", "0.0.1-SNAPSHOT");
        return ResponseEntity.ok(response);
    }
}

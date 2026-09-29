package com.medix.orders.web;

import com.medix.orders.external.ExternalClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ExternalClients.InsurerQuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleQuota(
            ExternalClients.InsurerQuotaExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "insurer_quota_exceeded",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "bad_request",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "invalid_state",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()));
    }
}

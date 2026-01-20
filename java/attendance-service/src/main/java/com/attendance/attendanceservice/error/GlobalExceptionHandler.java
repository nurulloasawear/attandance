package com.attendance.attendanceservice.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex, HttpServletRequest req) {
        String tid = traceId();

        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", ex.getStatus().value(),
                "code", ex.getCode(),
                "message", ex.getMessage(),
                "path", req.getRequestURI(),
                "traceId", tid,
                "details", ex.getDetails()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex, HttpServletRequest req) {
        String tid = traceId();

        return ResponseEntity.status(500).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 500,
                "code", "INTERNAL_ERROR",
                "message", "Unexpected error",
                "path", req.getRequestURI(),
                "traceId", tid,
                "details", Map.of()
        ));
    }

    private String traceId() {
        String tid = MDC.get("traceId");
        if (tid == null) tid = UUID.randomUUID().toString();
        return tid;
    }
}

package com.attendance.userservice.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest req) {
        String tid = traceId();

        // 4xx — обычно warn
        if (ex.getStatus().is4xxClientError()) {
            log.warn("API error: status={} code={} message={} path={} traceId={}",
                    ex.getStatus().value(), ex.getCode(), ex.getMessage(), req.getRequestURI(), tid);
        } else {
            log.error("API error (5xx): status={} code={} message={} path={} traceId={}",
                    ex.getStatus().value(), ex.getCode(), ex.getMessage(), req.getRequestURI(), tid);
        }

        return ResponseEntity
                .status(ex.getStatus())
                .body(apiError(ex.getStatus(), ex.getCode(), ex.getMessage(), req.getRequestURI(), tid, ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String tid = traceId();

        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));

        log.warn("Validation failed: path={} traceId={} fields={}", req.getRequestURI(), tid, fields);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError(
                        HttpStatus.BAD_REQUEST,
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        req.getRequestURI(),
                        tid,
                        Map.of("fields", fields)
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDb(DataIntegrityViolationException ex, HttpServletRequest req) {
        String tid = traceId();

        log.warn("DB conflict: path={} traceId={} msg={}", req.getRequestURI(), tid, ex.getMostSpecificCause().getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError(
                        HttpStatus.CONFLICT,
                        ErrorCode.CONFLICT,
                        "Database conflict",
                        req.getRequestURI(),
                        tid,
                        Map.of("cause", "DataIntegrityViolationException")
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
        String tid = traceId();

        log.error("Unexpected error: path={} traceId={}", req.getRequestURI(), tid, ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.INTERNAL_ERROR,
                        "Unexpected error",
                        req.getRequestURI(),
                        tid,
                        Map.of()
                ));
    }


    private static ApiError apiError(
            HttpStatus status,
            ErrorCode code,
            String message,
            String path,
            String traceId,
            Map<String, Object> details
    ) {
        return new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                path,
                traceId,
                details == null ? Map.of() : details
        );
    }

    private static String traceId() {
        String tid = MDC.get("traceId");
        if (tid == null || tid.isBlank()) {
            tid = UUID.randomUUID().toString();
            MDC.put("traceId", tid);
        }
        return tid;
    }
}

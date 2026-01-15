package com.attendance.userservice.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(
                        Instant.now(),
                        ex.getStatus().value(),
                        ex.getCode(),
                        ex.getMessage(),
                        req.getRequestURI(),
                        UUID.randomUUID().toString(),
                        ex.getDetails()
                ));
    }

    // @Valid body -> ошибки полей
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));

        return ResponseEntity.unprocessableEntity()
                .body(new ApiError(
                        Instant.now(),
                        422,
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        req.getRequestURI(),
                        UUID.randomUUID().toString(),
                        Map.of("fields", fields)
                ));
    }

    // База данных: UNIQUE constraint и т.п.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDb(DataIntegrityViolationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        Instant.now(),
                        409,
                        ErrorCode.CONFLICT,
                        "Database conflict (unique constraint etc.)",
                        req.getRequestURI(),
                        UUID.randomUUID().toString(),
                        Map.of("cause", "DataIntegrityViolationException")
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        Instant.now(),
                        500,
                        ErrorCode.INTERNAL_ERROR,
                        "Unexpected error",
                        req.getRequestURI(),
                        UUID.randomUUID().toString(),
                        Map.of()
                ));
    }
}

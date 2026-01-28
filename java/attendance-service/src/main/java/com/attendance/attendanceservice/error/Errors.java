package com.attendance.attendanceservice.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public final class Errors {

    private Errors() {}

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, Map.of());
    }

    public static ApiException badRequest(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, details);
    }

    public static ApiException validation(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, details);
    }

    // ========== 401 =========
    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message, Map.of());
    }

    public static ApiException tokenInvalid(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", message, Map.of());
    }

    public static ApiException tokenExpired(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", message, Map.of());
    }

    // ========== 403 ==========
    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message, Map.of());
    }

    public static ApiException forbidden(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message, details);
    }

    // ========== 404 ==========
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message, Map.of());
    }

    public static ApiException notFound(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message, details);
    }

    // ========== 409 ==========
    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message, Map.of());
    }

    public static ApiException conflict(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message, details);
    }

    // ========== 500 ==========
    public static ApiException internal(String message) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, Map.of());
    }

    public static ApiException internal(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, details);
    }
}

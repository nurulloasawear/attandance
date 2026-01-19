package com.attendance.userservice.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public final class Errors {

    private Errors() {}

    public static ApiException badRequest(String msg) {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                msg
        );
    }

    public static ApiException validation(String msg, Map<String, Object> details) {
        return new ApiException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorCode.VALIDATION_ERROR,
                msg,
                details
        );
    }

    public static ApiException notFound(String msg, Map<String, Object> details) {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                msg,
                details
        );
    }

    public static ApiException conflict(String msg, Map<String, Object> details) {
        return new ApiException(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                msg,
                details
        );
    }

    public static ApiException unauthorized(String msg) {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                msg
        );
    }

    public static ApiException forbidden(String msg) {
        return new ApiException(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN,
                msg
        );
    }

    public static ApiException forbidden(String msg, Map<String, Object> details) {
        return new ApiException(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN,
                msg,
                details
        );
    }

    public static ApiException tokenInvalid(String msg) {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.TOKEN_INVALID,
                msg
        );
    }

    public static ApiException tokenExpired(String msg) {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.TOKEN_EXPIRED,
                msg
        );
    }

    public static ApiException internal(String msg) {
        return new ApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                msg
        );
    }
}

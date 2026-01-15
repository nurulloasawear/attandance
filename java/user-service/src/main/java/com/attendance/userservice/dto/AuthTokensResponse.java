package com.attendance.userservice.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String sessionId,
        String publicId
) {}

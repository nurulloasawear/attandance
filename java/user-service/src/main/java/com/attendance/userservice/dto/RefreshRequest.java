package com.attendance.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(String refreshToken, String sessionId) {}


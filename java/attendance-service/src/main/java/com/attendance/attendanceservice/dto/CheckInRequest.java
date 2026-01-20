package com.attendance.attendanceservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CheckInRequest(
        @NotBlank
        @Size(min = 8, max = 8)
        String userPublicId,

        Instant time,
        String note
) {}

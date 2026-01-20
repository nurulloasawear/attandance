package com.attendance.attendanceservice.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordDto(
        UUID id,
        UUID userId,
        String userPublicId,
        LocalDate workDate,
        Instant checkIn,
        Instant checkOut,
        String status,
        String note
) {}

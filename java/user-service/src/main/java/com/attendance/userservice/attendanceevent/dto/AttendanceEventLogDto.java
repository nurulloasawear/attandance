package com.attendance.userservice.attendanceevent.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record  AttendanceEventLogDto(
        UUID id,
        String eventId,
        String type,
        UUID recordId,
        UUID userId,
        String userPublicId,
        LocalDate workDate,
        Instant occurredAt,
        Instant receivedAt
) {}

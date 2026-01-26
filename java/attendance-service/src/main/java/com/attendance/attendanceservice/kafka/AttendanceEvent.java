package com.attendance.attendanceservice.kafka;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceEvent(
        String eventId,
        String type,
        UUID recordId,
        UUID userId,
        String userPublicId,
        LocalDate workDate,
        Instant occurredAt
) {}

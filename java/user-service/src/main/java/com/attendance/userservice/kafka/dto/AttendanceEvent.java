package com.attendance.userservice.kafka.dto;

import java.time.Instant;

public record AttendanceEvent(
        String eventId,
        String type,
        String recordId,
        String userPublicId,
        Instant time
) {}

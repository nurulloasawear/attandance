package com.attendance.userservice.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        ErrorCode code,
        String message,
        String path,
        String traceId,
        Map<String, Object> details
) {}

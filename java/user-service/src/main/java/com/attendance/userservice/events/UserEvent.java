package com.attendance.userservice.events;

import com.attendance.userservice.model.audit.UserAction;

import java.time.Instant;
import java.util.UUID;

public record UserEvent(
        UserAction action,
        UUID userId,
        String publicId,
        String username,
        String actor,
        String sid,
        Instant at
) {}

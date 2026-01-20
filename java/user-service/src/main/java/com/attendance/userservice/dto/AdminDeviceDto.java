package com.attendance.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AdminDeviceDto {
    private UUID id;

    private String userPublicId;
    private String username;
    private String role;

    private String deviceKey; // X-Device
    private String sessionId;
    private String ip;
    private String userAgent;

    private Instant firstSeenAt;
    private Instant lastSeenAt;

    private boolean banned;
    private Instant bannedAt;
    private String bannedReason;
}

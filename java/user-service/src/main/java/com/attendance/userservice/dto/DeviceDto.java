package com.attendance.userservice.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DeviceDto {
    private UUID id;
    private String userPublicId;
    private String sessionId;
    private String ip;
    private String userAgent;

    private Instant firstSeenAt;
    private Instant lastSeenAt;

    private boolean banned;
    private Instant bannedAt;
    private String bannedReason;
}

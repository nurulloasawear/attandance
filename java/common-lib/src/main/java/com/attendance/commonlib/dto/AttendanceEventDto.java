package com.attendance.commonlib.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEventDto {

    private UUID userId;
    private String eventType;
    private LocalDateTime timestamp;
    private String deviceId;
    private Double confidenceScore;
}

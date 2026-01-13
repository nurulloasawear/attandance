package com.attendance.commonlib.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AttendanceEventDto {

    private UUID userId;
    private String eventType;
    private LocalDateTime timestamp;
    private String deviceId;
    private Double confidenceScore;

    public AttendanceEventDto() {
    }

    public AttendanceEventDto(UUID userId, String eventType, LocalDateTime timestamp, String deviceId, Double confidenceScore) {
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.confidenceScore = confidenceScore;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}

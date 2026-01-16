package com.attendance.userservice.audit;

import com.attendance.userservice.model.audit.UserAction;

import java.util.UUID;

public interface AuditEventPublisher {
    void publish(UUID userId, UserAction action, String payloadJson);
}

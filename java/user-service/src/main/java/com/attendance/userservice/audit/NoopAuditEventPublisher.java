package com.attendance.userservice.audit;

import com.attendance.userservice.model.audit.UserAction;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NoopAuditEventPublisher implements AuditEventPublisher {
    @Override
    public void publish(UUID userId, UserAction action, String payloadJson) {
    }
}

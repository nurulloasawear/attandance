package com.attendance.userservice.service;

import com.attendance.userservice.model.User;
import com.attendance.userservice.model.audit.UserAction;
import com.attendance.userservice.model.audit.UserAuditLog;
import com.attendance.userservice.repository.UserAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditLogService {

    private final UserAuditLogRepository repo;

    public void log(User user, UserAction action, String actor, String sid, String ip, String device, String details) {
        UserAuditLog l = UserAuditLog.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .userPublicId(user.getPublicId())
                .action(action)
                .createdAt(Instant.now())
                .actor(actor)
                .sid(sid)
                .ip(ip)
                .device(device)
                .details(details)
                .build();
        repo.save(l);
    }
}

package com.attendance.userservice.audit;

import com.attendance.userservice.model.audit.UserAction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditLogService {

    private final JdbcTemplate jdbc;

    public void log(UUID userId, UserAction action, AuditContext ctx, String message) {
        jdbc.update("""
            INSERT INTO user_audit_logs
                (id, user_id, action, actor_public_id, session_id, ip, device, message)
            VALUES
                (?, ?, ?, ?, ?, ?, ?, ?)
        """,
                UUID.randomUUID(),
                userId,
                action.name(),
                ctx == null ? null : ctx.actorPublicId(),
                ctx == null ? null : ctx.sessionId(),
                ctx == null ? null : ctx.ip(),
                ctx == null ? null : ctx.device(),
                message
        );
    }
}

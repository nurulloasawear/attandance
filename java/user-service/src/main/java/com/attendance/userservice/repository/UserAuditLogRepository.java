package com.attendance.userservice.repository;

import com.attendance.userservice.model.audit.UserAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, UUID> {
    List<UserAuditLog> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}

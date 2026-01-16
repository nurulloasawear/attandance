package com.attendance.userservice.model.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_audit_logs")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditLog {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String userPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UserAction action;

    @Column(nullable = false)
    private Instant createdAt;

    private String actor;

    private String sid;
    private String ip;
    private String device;

    @Column(length = 1000)
    private String details;
}

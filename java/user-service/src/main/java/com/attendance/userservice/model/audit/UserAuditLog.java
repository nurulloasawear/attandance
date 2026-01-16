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

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_public_id")
    private String userPublicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private UserAction action;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "actor")
    private String actor;

    @Column(name = "sid")
    private String sid;

    @Column(name = "ip")
    private String ip;

    @Column(name = "device")
    private String device;

    @Column(name = "details", columnDefinition = "text")
    private String details;
}


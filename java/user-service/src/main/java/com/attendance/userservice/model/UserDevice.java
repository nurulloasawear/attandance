package com.attendance.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_devices")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "ip")
    private String ip;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "banned", nullable = false)
    private boolean banned;

    @Column(name = "banned_at")
    private Instant bannedAt;

    @Column(name = "banned_reason", columnDefinition = "TEXT")
    private String bannedReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.firstSeenAt == null) this.firstSeenAt = now;
        if (this.lastSeenAt == null) this.lastSeenAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

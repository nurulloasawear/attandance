package com.attendance.userservice.attendanceevent.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance_event_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_event_id", columnNames = {"event_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEventLog {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "record_id", columnDefinition = "uuid")
    private UUID recordId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "user_public_id", nullable = false, length = 64)
    private String userPublicId;

    @Column(name = "work_date")
    private LocalDate workDate;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "actor_public_id", length = 64)
    private String actorPublicId;

    @Column(name = "actor_role", length = 64)
    private String actorRole;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) receivedAt = Instant.now();
    }
}

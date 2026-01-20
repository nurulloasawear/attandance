package com.attendance.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_faces",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_faces_user_id", columnNames = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "face_data", nullable = false, columnDefinition = "bytea")
    private byte[] faceData;

    @Column(name = "format", nullable = false, length = 50)
    private String format;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.format == null || this.format.isBlank()) {
            this.format = "FACE_TEMPLATE_V1";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();

        if (this.format == null || this.format.isBlank()) {
            this.format = "FACE_TEMPLATE_V1";
        }
    }
}

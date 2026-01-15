package com.attendance.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_faces",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_faces_user_id", columnNames = "user_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserFace {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "face_data", nullable = false)
    private byte[] faceData;

    @Column(name = "format", nullable = false, length = 50)
    private String format;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

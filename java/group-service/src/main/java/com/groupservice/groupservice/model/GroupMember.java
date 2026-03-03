package com.groupservice.groupservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_members")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GroupMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)

    @Column(name = "user_public_id", nullable = false, unique = true, length = 64)
    private String userPublicId;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
}
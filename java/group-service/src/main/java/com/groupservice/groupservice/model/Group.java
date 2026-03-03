package com.groupservice.groupservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Group {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "group_public_id", nullable = false, unique = true, length = 32)
    private String groupPublicId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String department;

    @Column(name = "leader_user_public_id", length = 64)
    private String leaderUserPublicId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
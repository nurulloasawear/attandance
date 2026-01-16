package com.attendance.userservice.model;

import com.attendance.userservice.model.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "users")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseAuditEntity {

    @Column(name="public_id", length=8, nullable=false, unique=true)
    private String publicId;

    @Column(nullable=false, unique=true)
    private String username;

    @Column(nullable=false)
    private String password;

    @Column(nullable=false, unique=true)
    private String email;

    private String firstName;
    private String lastName;

    @Column(nullable=false)
    private String role;

    @Column(name="is_active", nullable=false)
    private boolean active;
}

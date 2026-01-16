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
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW(), updated_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseAuditEntity {

    @Column(name = "public_id", length = 8, nullable = false)
    private String publicId;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", length = 200, nullable = false)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "role", length = 50, nullable = false)
    private String role;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}

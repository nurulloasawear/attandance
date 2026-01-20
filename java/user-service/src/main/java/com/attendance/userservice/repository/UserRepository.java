package com.attendance.userservice.repository;

import com.attendance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByDeletedAtIsNull();
    List<User> findAllByRoleIgnoreCaseAndDeletedAtIsNull(String role);

    Optional<User> findByPublicId(String publicId);
    boolean existsByPublicId(String publicId);
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    Optional<User> findByPublicIdAndDeletedAtIsNull(String publicId);

    boolean existsByIdAndDeletedAtIsNull(UUID id);
    boolean existsByUsernameAndDeletedAtIsNull(String username);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByPublicIdAndDeletedAtIsNull(String publicId);
    void deleteByUsername(String username);
    void deleteByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

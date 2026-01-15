package com.attendance.userservice.repository;

import com.attendance.userservice.model.UserFace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserFaceRepository extends JpaRepository<UserFace, UUID> {
    Optional<UserFace> findByUser_PublicId(String publicId);
    boolean existsByUser_PublicId(String publicId);
    void deleteByUser_PublicId(String publicId);
}

package com.attendance.userservice.repository;

import com.attendance.userservice.model.UserFace;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserFaceRepository extends JpaRepository<UserFace, UUID> {

    Optional<UserFace> findByUser_PublicId(String publicId);

    boolean existsByUser_PublicId(String publicId);

    void deleteByUser_PublicId(String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select uf from UserFace uf
        where uf.user.publicId = :publicId
    """)
    Optional<UserFace> findForUpdateByUserPublicId(@Param("publicId") String publicId);
}

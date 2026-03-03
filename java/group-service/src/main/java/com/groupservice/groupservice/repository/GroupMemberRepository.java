package com.groupservice.groupservice.repository;

import com.groupservice.groupservice.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    Optional<GroupMember> findByUserPublicId(String userPublicId);
    List<GroupMember> findAllByGroup_Id(UUID groupId);
    long countByGroup_Id(UUID groupId);
    void deleteByGroup_IdAndUserPublicId(UUID groupId, String userPublicId);
}
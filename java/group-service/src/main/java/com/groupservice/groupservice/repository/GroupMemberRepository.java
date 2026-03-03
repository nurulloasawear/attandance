package com.groupservice.groupservice.repository;

import com.groupservice.groupservice.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findAllByGroup_GroupPublicIdOrderByJoinedAtAsc(String groupPublicId);
    Optional<GroupMember> findByUserPublicId(String userPublicId);
    void deleteByUserPublicId(String userPublicId);
    void deleteByGroup_GroupPublicIdAndUserPublicId(String groupPublicId, String userPublicId);
}
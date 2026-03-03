package com.groupservice.groupservice.repository;

import com.groupservice.groupservice.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findByGroupPublicId(String groupPublicId);
    boolean existsByGroupPublicId(String groupPublicId);

    // ✅ to enforce "one leader -> one group"
    boolean existsByLeaderUserPublicId(String leaderUserPublicId);
}
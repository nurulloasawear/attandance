package com.attendance.userservice.repository;

import com.attendance.userservice.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    List<UserDevice> findAllByOrderByLastSeenAtDesc();

    Optional<UserDevice> findBySessionId(String sessionId);

    boolean existsBySessionIdAndBannedTrue(String sessionId);
}

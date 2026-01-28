package com.attendance.attendanceservice.repository;

import com.attendance.attendanceservice.model.AttendanceEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AttendanceEventLogRepository extends JpaRepository<AttendanceEventLog, UUID> {
    Optional<AttendanceEventLog> findByEventId(String eventId);
}

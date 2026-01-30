package com.attendance.userservice.attendanceevent.repository;

import com.attendance.userservice.attendanceevent.model.AttendanceEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceEventLogRepository extends JpaRepository<AttendanceEventLog, UUID> {
    Optional<AttendanceEventLog> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
    List<AttendanceEventLog> findAllByUserPublicIdAndWorkDateBetweenOrderByWorkDateDescOccurredAtDesc(
            String userPublicId,
            LocalDate from,
            LocalDate to
    );
}

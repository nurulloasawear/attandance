package com.attendance.attendanceservice.repository;

import com.attendance.attendanceservice.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByUserIdAndWorkDate(UUID userId, LocalDate workDate);

    List<AttendanceRecord> findAllByUserPublicIdAndWorkDateBetween(
            String userPublicId,
            LocalDate from,
            LocalDate to
    );

    List<AttendanceRecord> findAllByWorkDate(LocalDate date);
}

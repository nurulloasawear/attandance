package com.attendance.userservice.attendanceevent.service;

import com.attendance.commonlib.kafka.AttendanceEvent;
import com.attendance.userservice.attendanceevent.model.AttendanceEventLog;
import com.attendance.userservice.attendanceevent.repository.AttendanceEventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceEventLogService {

    private final AttendanceEventLogRepository repo;

    @Transactional
    public void handle(AttendanceEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()) return;

        if (repo.existsByEventId(event.eventId())) return;

        AttendanceEventLog log = AttendanceEventLog.builder()
                .eventId(event.eventId())
                .type(event.type())
                .recordId(event.recordId())
                .userId(event.userId())
                .userPublicId(event.userPublicId())
                .workDate(event.workDate())
                .occurredAt(event.occurredAt())
                .actorPublicId(event.actorPublicId())
                .actorRole(event.actorRole())
                .build();

        try {
            repo.save(log);
        } catch (DataIntegrityViolationException ignored) {
        }
    }
}

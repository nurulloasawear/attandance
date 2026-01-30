package com.attendance.userservice.attendanceevent.service;

import com.attendance.userservice.attendanceevent.dto.AttendanceEventLogDto;
import com.attendance.userservice.attendanceevent.model.AttendanceEventLog;
import com.attendance.userservice.attendanceevent.repository.AttendanceEventLogRepository;
import com.attendance.userservice.error.Errors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceEventQueryService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final int DEFAULT_DAYS = 30;

    private final AttendanceEventLogRepository repo;

    @Transactional(readOnly = true)
    public List<AttendanceEventLogDto> history(
            String targetPublicId,
            LocalDate from,
            LocalDate to,
            String actorPublicId,
            String actorRole
    ) {
        if (targetPublicId == null || targetPublicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }
        if (actorPublicId == null || actorPublicId.isBlank()) {
            throw Errors.unauthorized("Missing actor");
        }

        boolean isSelf = targetPublicId.equals(actorPublicId);
        boolean isAdmin = ROLE_ADMIN.equals(actorRole) || ROLE_MANAGER.equals(actorRole);

        if (!isSelf && !isAdmin) {
            throw Errors.forbidden("Forbidden");
        }

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        LocalDate safeTo = (to != null) ? to : todayUtc;
        LocalDate safeFrom = (from != null) ? from : safeTo.minusDays(DEFAULT_DAYS);

        if (safeFrom.isAfter(safeTo)) {
            throw Errors.badRequest("from must be <= to");
        }

        return repo.findAllByUserPublicIdAndWorkDateBetweenOrderByWorkDateDescOccurredAtDesc(
                        targetPublicId, safeFrom, safeTo
                )
                .stream()
                .map(this::map)
                .toList();
    }

    private AttendanceEventLogDto map(AttendanceEventLog e) {
        return new AttendanceEventLogDto(
                e.getId(),
                e.getEventId(),
                e.getType(),
                e.getRecordId(),
                e.getUserId(),
                e.getUserPublicId(),
                e.getWorkDate(),
                e.getOccurredAt(),
                e.getReceivedAt()
        );
    }
}

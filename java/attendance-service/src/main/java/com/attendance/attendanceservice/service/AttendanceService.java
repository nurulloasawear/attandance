package com.attendance.attendanceservice.service;

import com.attendance.attendanceservice.dto.AttendanceRecordDto;
import com.attendance.attendanceservice.dto.CheckInRequest;
import com.attendance.attendanceservice.dto.CheckOutRequest;
import com.attendance.attendanceservice.dto.UserInfoDto;
import com.attendance.attendanceservice.error.Errors;
import com.attendance.attendanceservice.kafka.AttendanceEvent;
import com.attendance.attendanceservice.kafka.AttendanceEventPublisher;
import com.attendance.attendanceservice.model.AttendanceRecord;
import com.attendance.attendanceservice.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final String STATUS_PRESENT = "PRESENT";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    private static final String EVENT_CHECK_IN = "CHECK_IN";
    private static final String EVENT_CHECK_OUT = "CHECK_OUT";

    private final AttendanceRecordRepository repo;
    private final UserClient userClient;
    private final AttendanceEventPublisher eventPublisher;

    @Transactional
    public AttendanceRecordDto checkIn(CheckInRequest req, String actorPublicId, String actorRole) {
        validateRequest(req.userPublicId());
        enforceSelfOrAdmin(req.userPublicId(), actorPublicId, actorRole);

        UserInfoDto user = loadActiveUser(req.userPublicId());

        Instant now = resolveNow(req.time());
        LocalDate workDate = LocalDate.ofInstant(now, ZoneOffset.UTC);

        AttendanceRecord record = repo.findByUserIdAndWorkDate(user.id(), workDate)
                .orElseGet(() -> AttendanceRecord.builder()
                        .userId(user.id())
                        .userPublicId(user.publicId())
                        .workDate(workDate)
                        .status(STATUS_PRESENT)
                        .createdBy(actorPublicId)
                        .build());

        if (record.getCheckIn() != null) {
            throw Errors.conflict("Already checked in");
        }

        record.setCheckIn(now);
        record.setNote(req.note());
        record.setUpdatedBy(actorPublicId);

        AttendanceRecord saved = repo.save(record);

        eventPublisher.publish(new AttendanceEvent(
                UUID.randomUUID().toString(),
                EVENT_CHECK_IN,
                saved.getId(),
                saved.getUserId(),
                saved.getUserPublicId(),
                saved.getWorkDate(),
                saved.getCheckIn()
        ));

        return map(saved);
    }

    @Transactional
    public AttendanceRecordDto checkOut(CheckOutRequest req, String actorPublicId, String actorRole) {
        validateRequest(req.userPublicId());
        enforceSelfOrAdmin(req.userPublicId(), actorPublicId, actorRole);

        UserInfoDto user = loadActiveUser(req.userPublicId());

        Instant now = resolveNow(req.time());
        LocalDate workDate = LocalDate.ofInstant(now, ZoneOffset.UTC);

        AttendanceRecord record = repo.findByUserIdAndWorkDate(user.id(), workDate)
                .orElseThrow(() -> Errors.notFound("No record for today"));

        if (record.getCheckIn() == null) {
            throw Errors.conflict("Not checked in yet");
        }

        if (record.getCheckOut() != null) {
            throw Errors.conflict("Already checked out");
        }

        record.setCheckOut(now);
        record.setUpdatedBy(actorPublicId);

        AttendanceRecord saved = repo.save(record);

        eventPublisher.publish(new AttendanceEvent(
                UUID.randomUUID().toString(),
                EVENT_CHECK_OUT,
                saved.getId(),
                saved.getUserId(),
                saved.getUserPublicId(),
                saved.getWorkDate(),
                saved.getCheckOut()
        ));

        return map(saved);
    }

    @Transactional(readOnly = true)
    public AttendanceRecordDto today(String targetUserPublicId, String actorPublicId, String actorRole) {
        validateRequest(targetUserPublicId);
        enforceSelfOrAdmin(targetUserPublicId, actorPublicId, actorRole);

        UserInfoDto user = loadActiveUser(targetUserPublicId);

        LocalDate workDate = LocalDate.now(ZoneOffset.UTC);

        AttendanceRecord record = repo.findByUserIdAndWorkDate(user.id(), workDate)
                .orElseThrow(() -> Errors.notFound("No record for today"));

        return map(record);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> history(String targetUserPublicId, LocalDate from, LocalDate to, String actorPublicId, String actorRole) {
        validateRequest(targetUserPublicId);
        enforceSelfOrAdmin(targetUserPublicId, actorPublicId, actorRole);

        UserInfoDto user = loadActiveUser(targetUserPublicId);

        LocalDate safeFrom = from != null ? from : LocalDate.now(ZoneOffset.UTC).minusDays(30);
        LocalDate safeTo = to != null ? to : LocalDate.now(ZoneOffset.UTC);

        if (safeFrom.isAfter(safeTo)) {
            throw Errors.badRequest("from must be <= to");
        }

        return repo.findAllByUserIdAndWorkDateBetweenOrderByWorkDateDesc(user.id(), safeFrom, safeTo)
                .stream()
                .map(this::map)
                .toList();
    }

    private void validateRequest(String userPublicId) {
        if (userPublicId == null || userPublicId.isBlank()) {
            throw Errors.badRequest("userPublicId is required");
        }
    }

    private UserInfoDto loadActiveUser(String publicId) {
        UserInfoDto user = userClient.getUserByPublicId(publicId);
        if (user == null) {
            throw Errors.notFound("User not found");
        }
        if (!user.active()) {
            throw Errors.forbidden("User disabled");
        }
        return user;
    }

    private Instant resolveNow(Instant requestTime) {
        return requestTime != null ? requestTime : Instant.now();
    }

    private void enforceSelfOrAdmin(String targetPublicId, String actorPublicId, String actorRole) {
        if (actorRole == null || actorRole.isBlank()) {
            throw Errors.unauthorized("Missing role");
        }
        if (actorPublicId == null || actorPublicId.isBlank()) {
            throw Errors.unauthorized("Missing actor");
        }

        boolean isAdmin = ROLE_ADMIN.equals(actorRole) || ROLE_MANAGER.equals(actorRole);
        boolean isSelf = targetPublicId.equals(actorPublicId);

        if (!isAdmin && !isSelf) {
            throw Errors.forbidden("You can mark attendance only for yourself");
        }
    }

    private AttendanceRecordDto map(AttendanceRecord r) {
        return new AttendanceRecordDto(
                r.getId(),
                r.getUserId(),
                r.getUserPublicId(),
                r.getWorkDate(),
                r.getCheckIn(),
                r.getCheckOut(),
                r.getStatus(),
                r.getNote()
        );
    }
}

package com.attendance.attendanceservice.service;

import com.attendance.attendanceservice.dto.AttendanceRecordDto;
import com.attendance.attendanceservice.dto.CheckInRequest;
import com.attendance.attendanceservice.dto.CheckOutRequest;
import com.attendance.attendanceservice.dto.UserInfoDto;
import com.attendance.attendanceservice.error.Errors;
import com.attendance.attendanceservice.model.AttendanceRecord;
import com.attendance.attendanceservice.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository repo;
    private final UserClient userClient;

    @Transactional
    public AttendanceRecordDto checkIn(CheckInRequest req, String actorPublicId, String actorRole) {

        enforceSelfOrAdmin(req.userPublicId(), actorPublicId, actorRole);

        UserInfoDto user = userClient.getUserByPublicId(req.userPublicId());
        if (!user.active()) throw Errors.forbidden("User disabled");

        Instant now = (req.time() != null) ? req.time() : Instant.now();
        LocalDate workDate = LocalDate.ofInstant(now, ZoneId.systemDefault());

        AttendanceRecord record = repo.findByUserIdAndWorkDate(user.id(), workDate)
                .orElse(AttendanceRecord.builder()
                        .userId(user.id())
                        .userPublicId(user.publicId())
                        .workDate(workDate)
                        .status("PRESENT")
                        .createdBy(actorPublicId)
                        .build());

        if (record.getCheckIn() != null) {
            throw Errors.conflict("Already checked in");
        }

        record.setCheckIn(now);
        record.setNote(req.note());
        record.setUpdatedBy(actorPublicId);

        AttendanceRecord saved = repo.save(record);
        return map(saved);
    }

    @Transactional
    public AttendanceRecordDto checkOut(CheckOutRequest req, String actorPublicId, String actorRole) {

        enforceSelfOrAdmin(req.userPublicId(), actorPublicId, actorRole);

        UserInfoDto user = userClient.getUserByPublicId(req.userPublicId());
        if (!user.active()) throw Errors.forbidden("User disabled");

        Instant now = (req.time() != null) ? req.time() : Instant.now();
        LocalDate workDate = LocalDate.ofInstant(now, ZoneId.systemDefault());

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
        return map(saved);
    }

    private void enforceSelfOrAdmin(String targetPublicId, String actorPublicId, String actorRole) {
        if (actorRole == null) throw Errors.unauthorized("Missing role");

        boolean isAdmin = actorRole.equals("ROLE_ADMIN") || actorRole.equals("ROLE_MANAGER");
        boolean isSelf = targetPublicId != null && targetPublicId.equals(actorPublicId);

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

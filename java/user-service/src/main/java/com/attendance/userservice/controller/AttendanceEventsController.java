package com.attendance.userservice.controller;

import com.attendance.userservice.attendanceevent.dto.AttendanceEventLogDto;
import com.attendance.userservice.attendanceevent.service.AttendanceEventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/events", produces = "application/json")
@RequiredArgsConstructor
public class AttendanceEventsController {

    private final AttendanceEventQueryService queryService;

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceEventLogDto>> attendanceEvents(
            @RequestParam(required = false) String userPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String actorPublicId = jwt.getClaimAsString("uid");
        String role = jwt.getClaimAsString("role");

        String target = (userPublicId == null || userPublicId.isBlank()) ? actorPublicId : userPublicId;

        return ResponseEntity.ok(queryService.history(target, from, to, actorPublicId, role));
    }
}

package com.attendance.attendanceservice.controller;

import com.attendance.attendanceservice.dto.AttendanceRecordDto;
import com.attendance.attendanceservice.dto.CheckInRequest;
import com.attendance.attendanceservice.dto.CheckOutRequest;
import com.attendance.attendanceservice.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public AttendanceRecordDto checkIn(@Valid @RequestBody CheckInRequest req, Authentication auth) {
        String actorPublicId = actorPublicId(auth);
        String role = actorRole(auth);
        return attendanceService.checkIn(req, actorPublicId, role);
    }

    @PostMapping("/check-out")
    public AttendanceRecordDto checkOut(@Valid @RequestBody CheckOutRequest req, Authentication auth) {
        String actorPublicId = actorPublicId(auth);
        String role = actorRole(auth);
        return attendanceService.checkOut(req, actorPublicId, role);
    }

    @GetMapping("/me/today")
    public AttendanceRecordDto myToday(Authentication auth) {
        String actorPublicId = actorPublicId(auth);
        String role = actorRole(auth);
        return attendanceService.today(actorPublicId, actorPublicId, role);
    }

    @GetMapping("/me/history")
    public List<AttendanceRecordDto> myHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth
    ) {
        String actorPublicId = actorPublicId(auth);
        String role = actorRole(auth);
        return attendanceService.history(actorPublicId, from, to, actorPublicId, role);
    }

    @GetMapping("/users/{publicId}/today")
    public AttendanceRecordDto userToday(@PathVariable String publicId, Authentication auth) {
        String actorPublicId = actorPublicId(auth);
        String role = actorRole(auth);
        return attendanceService.today(publicId, actorPublicId, role);
    }

    @GetMapping("/users/{publicId}/history")
    public List<AttendanceRecordDto> userHistory(
            @PathVariable String publicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth
    ) {
        String actorPublicId = actorPublicId(auth);
        String role = actorRole(auth);
        return attendanceService.history(publicId, from, to, actorPublicId, role);
    }

    private String actorPublicId(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new IllegalStateException("Missing authentication");
        }
        return auth.getName();
    }

    private String actorRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            throw new IllegalStateException("Missing role");
        }
        return auth.getAuthorities().iterator().next().getAuthority();
    }
}

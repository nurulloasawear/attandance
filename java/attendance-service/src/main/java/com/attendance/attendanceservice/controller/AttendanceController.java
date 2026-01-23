package com.attendance.attendanceservice.controller;

import com.attendance.attendanceservice.dto.AttendanceRecordDto;
import com.attendance.attendanceservice.dto.CheckInRequest;
import com.attendance.attendanceservice.dto.CheckOutRequest;
import com.attendance.attendanceservice.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public AttendanceRecordDto checkIn(
            @Valid @RequestBody CheckInRequest req,
            Authentication auth
    ) {
        String actorPublicId = (String) auth.getDetails();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return attendanceService.checkIn(req, actorPublicId, role);
    }

    @PostMapping("/check-out")
    public AttendanceRecordDto checkOut(
            @Valid @RequestBody CheckOutRequest req,
            Authentication auth
    ) {
        String actorPublicId = (String) auth.getDetails();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return attendanceService.checkOut(req, actorPublicId, role);
    }
}

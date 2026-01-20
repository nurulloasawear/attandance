package com.attendance.userservice.controller;

import com.attendance.userservice.dto.DeviceDto;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/devices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SUPER_ADMIN')")
public class AdminDeviceController {

    private final IUserService userService;


    @GetMapping
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        return ResponseEntity.ok(userService.getAllDevices());
    }


    @PatchMapping("/{deviceId}/ban")
    public ResponseEntity<?> banDevice(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) String reason
    ) {
        userService.banDevice(deviceId, reason);

        return ResponseEntity.ok(Map.of(
                "status", "BANNED",
                "deviceId", deviceId.toString(),
                "reason", (reason == null || reason.isBlank()) ? "banned" : reason
        ));
    }


    @PatchMapping("/{deviceId}/unban")
    public ResponseEntity<?> unbanDevice(@PathVariable UUID deviceId) {
        userService.unbanDevice(deviceId);

        return ResponseEntity.ok(Map.of(
                "status", "UNBANNED",
                "deviceId", deviceId.toString()
        ));
    }
}

package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.dto.DeviceDto;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserDto> create(
            @RequestBody UserDto dto,
            @RequestParam String password
    ) {
        return ResponseEntity.ok(userService.createUser(dto, password));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDto> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/by-publicId/{publicId}")
    public ResponseEntity<UserDto> getByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(userService.getUserByPublicId(publicId));
    }

    @GetMapping("/{id}/role")
    public ResponseEntity<String> getRoleById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserRoleById(id));
    }

    @GetMapping("/by-username/{username}/role")
    public ResponseEntity<String> getRoleByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserRoleByUsername(username));
    }

    @GetMapping("/by-publicId/{publicId}/role")
    public ResponseEntity<String> getRoleByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(userService.getUserRoleByPublicId(publicId));
    }

    @PatchMapping("/by-publicId/{publicId}")
    public ResponseEntity<UserDto> updateByPublicId(
            @PathVariable String publicId,
            @RequestBody UserDto dto,
            @RequestParam(required = false) String password,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String actorPublicId = jwt != null ? jwt.getClaimAsString("uid") : null;
        String sid = jwt != null ? jwt.getClaimAsString("sid") : null;

        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");

        return ResponseEntity.ok(
                userService.updateUserByPublicId(
                        publicId,
                        dto,
                        password,
                        actorPublicId,
                        sid,
                        ip,
                        device
                )
        );
    }

    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<?> deactivate(
            @PathVariable String publicId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String actorPublicId = jwt != null ? jwt.getClaimAsString("uid") : null;
        String sid = jwt != null ? jwt.getClaimAsString("sid") : null;

        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");

        userService.deactivateUserByPublicId(publicId, actorPublicId, sid, ip, device);

        return ResponseEntity.ok(Map.of(
                "status", "deactivated",
                "publicId", publicId
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id.toString()));
    }

    @DeleteMapping("/by-username/{username}")
    public ResponseEntity<?> deleteByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        return ResponseEntity.ok(Map.of("status", "deleted", "username", username));
    }

    @DeleteMapping("/by-email/{email}")
    public ResponseEntity<?> deleteByEmail(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok(Map.of("status", "deleted", "email", email));
    }

    @DeleteMapping("/by-publicId/{publicId}")
    public ResponseEntity<?> deleteByPublicId(@PathVariable String publicId) {
        userService.deleteUserByPublicId(publicId);
        return ResponseEntity.ok(Map.of("status", "deleted", "publicId", publicId));
    }

    @GetMapping("/roles")
    public ResponseEntity<String[]> getAllRoles() {
        return ResponseEntity.ok(
                Arrays.stream(RoleType.values())
                        .map(Enum::name)
                        .toArray(String[]::new)
        );
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        return ResponseEntity.ok(userService.getAllDevices());
    }

    @PatchMapping("/devices/{deviceId}/ban")
    public ResponseEntity<?> banDevice(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) String reason
    ) {
        userService.banDevice(deviceId, reason);
        return ResponseEntity.ok(Map.of(
                "status", "banned",
                "deviceId", deviceId.toString(),
                "reason", reason == null ? "banned" : reason
        ));
    }

    @PatchMapping("/devices/{deviceId}/unban")
    public ResponseEntity<?> unbanDevice(@PathVariable UUID deviceId) {
        userService.unbanDevice(deviceId);
        return ResponseEntity.ok(Map.of(
                "status", "unbanned",
                "deviceId", deviceId.toString()
        ));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

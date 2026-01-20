package com.attendance.userservice.controller;

import com.attendance.userservice.error.Errors;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public class SuperAdminController {

    private final IUserService userService;

    // ✅ Назначить роль (ADMIN / MANAGER)
    @PatchMapping("/users/{publicId}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable String publicId,
            @RequestParam String role,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String actorPublicId = jwt != null ? jwt.getClaimAsString("uid") : null;
        String sid = jwt != null ? jwt.getClaimAsString("sid") : null;

        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");

        userService.changeRoleBySuperAdmin(publicId, role, actorPublicId, sid, ip, device);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "publicId", publicId,
                "newRole", role.toUpperCase()
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

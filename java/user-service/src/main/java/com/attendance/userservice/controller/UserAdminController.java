package com.attendance.userservice.controller;

import com.attendance.userservice.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserServiceImpl userService;

    @PostMapping("/{publicId}/deactivate")
    public ResponseEntity<?> deactivate(
            @PathVariable String publicId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Device", required = false) String device,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip
    ) {
        String actorPublicId = jwt.getClaimAsString("publicId");
        String sid = jwt.getClaimAsString("sid");

        userService.deactivateUserByPublicId(publicId, actorPublicId, sid, ip, device);

        return ResponseEntity.ok(Map.of("status", "deactivated"));
    }
}

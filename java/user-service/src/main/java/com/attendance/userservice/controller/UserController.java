package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Missing or invalid access token"));
        }

        return ResponseEntity.ok(Map.of(
                "username", jwt.getSubject(),
                "uid", jwt.getClaimAsString("uid"),
                "role", jwt.getClaimAsString("role"),
                "sid", jwt.getClaimAsString("sid"),
                "jti", jwt.getClaimAsString("jti")
        ));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserDto> myProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String myPublicId = jwt.getClaimAsString("uid");
        return ResponseEntity.ok(userService.getMyProfile(myPublicId));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<UserDto> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserDto dto,
            @RequestParam(required = false) String password,
            HttpServletRequest request
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String myPublicId = jwt.getClaimAsString("uid");
        String sid = jwt.getClaimAsString("sid");

        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");

        UserDto updated = userService.updateMyProfile(
                myPublicId,
                dto,
                password,
                sid,
                ip,
                device
        );

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, Object>> deleteMyAccount(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Missing or invalid access token"));
        }

        String myPublicId = jwt.getClaimAsString("uid");
        String sid = jwt.getClaimAsString("sid");

        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");

        userService.deleteMyAccount(myPublicId, sid, ip, device);

        return ResponseEntity.ok(Map.of(
                "status", "deleted",
                "publicId", myPublicId
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

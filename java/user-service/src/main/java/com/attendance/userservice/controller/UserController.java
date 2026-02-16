package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/internal/users", produces = "application/json")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{publicId}")
    public ResponseEntity<UserDto> getByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(userService.getUserByPublicId(publicId));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Missing or invalid access token"));
        }

        return ResponseEntity.ok(Map.of(
                "username", jwt.getSubject(),
                "uid", jwt.getClaimAsString("uid"),
                "publicId", jwt.getClaimAsString("publicId"),
                "role", jwt.getClaimAsString("role"),
                "sid", jwt.getClaimAsString("sid"),
                "jti", jwt.getId()
        ));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserDto> myProfile(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UUID myId;
        try {
            myId = UUID.fromString(jwt.getClaimAsString("uid"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.getUserById(myId));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<UserDto> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserDto dto,
            @RequestParam(required = false) String password,
            HttpServletRequest request
    ) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UUID myId;
        try {
            myId = UUID.fromString(jwt.getClaimAsString("uid"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto me = userService.getUserById(myId);
        String myPublicId = me.getPublicId();
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

        UUID myId;
        try {
            myId = UUID.fromString(jwt.getClaimAsString("uid"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid uid in token"));
        }

        UserDto me = userService.getUserById(myId);
        String myPublicId = me.getPublicId();
        String sid = jwt.getClaimAsString("sid");

        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");

        userService.deleteMyAccount(myPublicId, sid, ip, device);

        return ResponseEntity.ok(Map.of(
                "status", "deleted",
                "id", myId.toString(),
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

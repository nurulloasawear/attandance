package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        return ResponseEntity.ok(userService.me(auth));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserDto> myProfile(Authentication auth) {
        return ResponseEntity.ok(userService.myProfile(auth));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<UserDto> updateMyProfile(
            Authentication auth,
            @RequestBody UserDto dto,
            @RequestParam(required = false) String password,
            HttpServletRequest request
    ) {
        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");
        return ResponseEntity.ok(userService.updateMyProfile(auth, dto, password, ip, device));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, Object>> deleteMyAccount(Authentication auth, HttpServletRequest request) {
        String ip = getClientIp(request);
        String device = request.getHeader("User-Agent");
        userService.deleteMyAccount(auth, ip, device);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
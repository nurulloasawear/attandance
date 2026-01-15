package com.attendance.userservice.controller;

import com.attendance.userservice.dto.*;
import com.attendance.userservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> register(
            @RequestBody @Valid RegisterRequest req,
            @RequestHeader(value = "X-Device", required = false) String device,
            HttpServletRequest request
    ) {
        String ip = extractClientIp(request);
        String finalDevice = (device == null || device.isBlank())
                ? request.getHeader("User-Agent")
                : device;

        AuthTokensResponse tokens = authService.register(req, finalDevice, ip);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(
            @RequestBody @Valid LoginRequest req,
            @RequestHeader(value = "X-Device", required = false) String device,
            HttpServletRequest request
    ) {
        String ip = extractClientIp(request);
        String finalDevice = (device == null || device.isBlank())
                ? request.getHeader("User-Agent")
                : device;

        AuthTokensResponse tokens = authService.login(req, finalDevice, ip);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        authService.logoutByAccessToken(authHeader);
        return ResponseEntity.ok().build();
    }

    private static String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

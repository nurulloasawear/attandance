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
        String ip = request.getRemoteAddr();
        AuthTokensResponse tokens = authService.register(req, device, ip);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/login")
    public AuthTokensResponse login(
            @RequestBody @Valid LoginRequest req,
            HttpServletRequest http
    ) {
        String device = http.getHeader("User-Agent");
        String ip = extractClientIp(http);
        return authService.login(req, device, ip);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@RequestBody @Valid RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
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

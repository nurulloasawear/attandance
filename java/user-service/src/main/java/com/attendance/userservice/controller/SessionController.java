package com.attendance.userservice.controller;

import com.attendance.userservice.error.Errors;
import com.attendance.userservice.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<?> mySessions(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) throw Errors.unauthorized("Missing or invalid access token");

        UUID userId = getUid(jwt);
        List<Map<Object, Object>> sessions = sessionService.listSessions(userId);

        sessions.sort((a, b) -> {
            String la = Objects.toString(a.get("lastSeen"), "");
            String lb = Objects.toString(b.get("lastSeen"), "");
            return lb.compareTo(la);
        });

        return ResponseEntity.ok(Map.of(
                "count", sessions.size(),
                "sessions", sessions
        ));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> revokeOne(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String sessionId
    ) {
        if (jwt == null) throw Errors.unauthorized("Missing or invalid access token");

        UUID userId = getUid(jwt);

        Map<Object, Object> data = sessionService.findSession(sessionId)
                .orElseThrow(() -> Errors.notFound("Session not found", Map.of("sessionId", sessionId)));

        String uidInSess = Objects.toString(data.get("uid"), null);
        if (uidInSess == null || !uidInSess.equals(userId.toString())) {
            throw Errors.forbidden("This session does not belong to you");
        }

        sessionService.revokeSession(userId, sessionId);

        return ResponseEntity.ok(Map.of("status", "revoked", "sessionId", sessionId));
    }

    @DeleteMapping("/others")
    public ResponseEntity<?> revokeOthers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) throw Errors.unauthorized("Missing or invalid access token");

        UUID userId = getUid(jwt);
        String currentSid = jwt.getClaimAsString("sid");
        if (currentSid == null || currentSid.isBlank()) {
            throw Errors.validation("Missing sid claim in token", Map.of());
        }

        int removed = sessionService.revokeAllExcept(userId, currentSid);

        return ResponseEntity.ok(Map.of(
                "status", "revoked_others",
                "keptSessionId", currentSid,
                "removed", removed
        ));
    }

    private static UUID getUid(Jwt jwt) {
        String uid = jwt.getClaimAsString("uid");
        if (uid == null || uid.isBlank()) {
            throw Errors.tokenInvalid("Token missing uid claim");
        }
        try {
            return UUID.fromString(uid);
        } catch (Exception e) {
            throw Errors.tokenInvalid("Invalid uid claim");
        }
    }
}

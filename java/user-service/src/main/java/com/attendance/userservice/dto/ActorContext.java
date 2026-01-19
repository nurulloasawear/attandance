package com.attendance.userservice.dto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

public record ActorContext(
        String actorPublicId,
        String sid,
        String ip,
        String device
) {
    public static ActorContext from(Jwt jwt, HttpServletRequest request) {
        String actorPublicId = jwt.getClaimAsString("uid");
        String sid = jwt.getClaimAsString("sid");

        String xff = request.getHeader("X-Forwarded-For");
        String ip = (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : request.getRemoteAddr();

        String device = request.getHeader("User-Agent");

        return new ActorContext(actorPublicId, sid, ip, device);
    }
}

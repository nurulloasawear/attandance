package com.attendance.userservice.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class JwtAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String publicId = jwt.getClaimAsString("publicId");
            if (publicId != null && !publicId.isBlank()) return Optional.of(publicId);

            // fallback
            String sub = jwt.getSubject();
            return Optional.ofNullable(sub);
        }
        return Optional.of(auth.getName());
    }
}

package com.attendance.userservice.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditorConfig {

    @Bean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a == null || !a.isAuthenticated() || a.getPrincipal() == null) {
                return Optional.of("system");
            }

            // Вариант 1: username
            return Optional.ofNullable(a.getName()).filter(s -> !s.isBlank()).or(() -> Optional.of("system"));

            // Вариант 2 (лучше): если у тебя principal хранит publicId — доставай оттуда
        };
    }
}

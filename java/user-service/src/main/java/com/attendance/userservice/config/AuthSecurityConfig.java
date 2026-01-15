package com.attendance.userservice.config;

import com.attendance.userservice.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain authChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/auth/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh"
                        ).permitAll()
                        // logout можно сделать protected (по Bearer)
                        .requestMatchers("/api/auth/logout").authenticated()
                        .anyRequest().denyAll()
                )
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}


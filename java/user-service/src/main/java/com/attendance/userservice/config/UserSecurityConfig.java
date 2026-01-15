package com.attendance.userservice.config;

import com.attendance.userservice.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class UserSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    @Order(2)
    public SecurityFilterChain userChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/users/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // swagger/actuator можно открыть (или закрыть — как хочешь)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()

                        // user api — только с токеном
                        .requestMatchers("/api/users/**").authenticated()

                        .anyRequest().denyAll()
                )
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

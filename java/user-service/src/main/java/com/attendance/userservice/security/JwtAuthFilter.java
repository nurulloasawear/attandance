package com.attendance.userservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String jti = jwtService.extractJti(token);
        if (jti == null || !redisTokenService.isAccessTokenActive(jti)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Claims claims = jwtService.parseClaims(token);

        Map<String, Object> claimsMap = new HashMap<>(claims);

        String uid = asString(claimsMap.get("uid"));               // иногда UUID
        String pid = asString(claimsMap.get("pid"));               // лучше держать так
        String publicId = asString(claimsMap.get("publicId"));     // или так

        String resolvedPublicId = firstNotBlank(pid, publicId);

        if (resolvedPublicId != null) {
            claimsMap.put("uid", resolvedPublicId);
            claimsMap.put("pid", resolvedPublicId);
        } else if (uid != null && uid.length() == 8) {
            claimsMap.put("pid", uid);
        }

        Instant iat = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : Instant.now();
        Instant exp = claims.getExpiration() != null ? claims.getExpiration().toInstant() : Instant.now().plusSeconds(60);

        Jwt jwt = Jwt.withTokenValue(token)
                .headers(h -> {
                    h.put("alg", "HS256");
                    h.put("typ", "JWT");
                })
                .claims(c -> c.putAll(claimsMap))
                .subject(claims.getSubject())
                .issuedAt(iat)
                .expiresAt(exp)
                .build();

        String role = asString(claimsMap.get("role")); // ROLE_EMPLOYEE / ROLE_MANAGER / ROLE_ADMIN

        List<GrantedAuthority> authorities = (role == null)
                ? List.of()
                : List.of(new SimpleGrantedAuthority(role));

        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private static String asString(Object v) {
        return v == null ? null : v.toString();
    }

    private static String firstNotBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}

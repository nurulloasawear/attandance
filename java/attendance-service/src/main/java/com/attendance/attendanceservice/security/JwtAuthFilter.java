package com.attendance.attendanceservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();

        if (HttpMethod.OPTIONS.matches(req.getMethod())) return true;

        return path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7).trim();

        if (token.isBlank() || !jwtService.isValid(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);
            Map<String, Object> claimsMap = new HashMap<>(claims);

            // --- нормализация publicId/pid/uid (как у user-service) ---
            String uid = asString(claimsMap.get("uid"));
            String pid = asString(claimsMap.get("pid"));
            String publicId = asString(claimsMap.get("publicId"));

            String resolvedPublicId = firstNotBlank(pid, publicId);

            if (resolvedPublicId != null) {
                claimsMap.put("uid", resolvedPublicId);
                claimsMap.put("pid", resolvedPublicId);
                claimsMap.put("publicId", resolvedPublicId);
            } else if (uid != null && uid.length() == 8) {
                claimsMap.put("pid", uid);
                claimsMap.put("publicId", uid);
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

            List<GrantedAuthority> authorities = resolveAuthorities(claimsMap);

            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, authorities));

            chain.doFilter(req, res);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private static List<GrantedAuthority> resolveAuthorities(Map<String, Object> claimsMap) {
        Object rolesObj = claimsMap.get("roles");
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (rolesObj instanceof Collection<?> col) {
            for (Object r : col) {
                String role = normalizeRole(asString(r));
                if (role != null) authorities.add(new SimpleGrantedAuthority(role));
            }
            return authorities;
        }

        String role = normalizeRole(asString(claimsMap.get("role")));
        if (role != null) authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    private static String normalizeRole(String role) {
        if (role == null) return null;
        role = role.trim();
        if (role.isEmpty()) return null;
        if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
        return role;
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

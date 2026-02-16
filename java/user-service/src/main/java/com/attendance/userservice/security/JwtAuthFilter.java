package com.attendance.userservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        if (token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
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

            // ВАЖНО: subject делаем = publicId, чтобы auth.getName() тоже был publicId
            String subject = firstNotBlank(asString(claimsMap.get("publicId")), claims.getSubject());

            Jwt jwt = Jwt.withTokenValue(token)
                    .headers(h -> {
                        h.put("alg", "HS256");
                        h.put("typ", "JWT");
                    })
                    .claims(c -> c.putAll(claimsMap))
                    .subject(subject)
                    .issuedAt(iat)
                    .expiresAt(exp)
                    .build();

            List<GrantedAuthority> authorities = extractAuthorities(claimsMap);

            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT filter failed: {} path={}", e.getMessage(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private static List<GrantedAuthority> extractAuthorities(Map<String, Object> claims) {
        List<GrantedAuthority> out = new ArrayList<>();

        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof Collection<?> col) {
            for (Object r : col) {
                String role = normalizeRole(asString(r));
                if (role != null) out.add(new SimpleGrantedAuthority(role));
            }
            return out;
        }

        String role = normalizeRole(asString(claims.get("role")));
        if (role != null) out.add(new SimpleGrantedAuthority(role));

        return out;
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

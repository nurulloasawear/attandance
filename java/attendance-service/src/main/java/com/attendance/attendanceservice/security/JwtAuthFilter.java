package com.attendance.attendanceservice.security;

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

            Claims claims = jwtService.extractAllClaims(token);
            Map<String, Object> claimsMap = new HashMap<>(claims);

            String publicId = trimToNull(claimsMap.get("publicId"));
            if (publicId == null) publicId = trimToNull(claimsMap.get("pid"));

            if (publicId == null) {
                String uid = trimToNull(claimsMap.get("uid"));
                if (uid != null && uid.length() == 8) publicId = uid;
            }

            if (publicId == null) {
                String sub = claims.getSubject();
                if (sub != null && !sub.isBlank() && sub.trim().length() == 8) publicId = sub.trim();
            }

            if (publicId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            claimsMap.put("publicId", publicId);
            claimsMap.put("pid", publicId);

            if (!claimsMap.containsKey("jti") && claims.getId() != null) {
                claimsMap.put("jti", claims.getId());
            }

            Instant iat = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : Instant.now();
            Instant exp = claims.getExpiration() != null ? claims.getExpiration().toInstant() : Instant.now().plusSeconds(60);

            Jwt jwt = Jwt.withTokenValue(token)
                    .headers(h -> {
                        h.put("typ", "JWT");
                        h.put("alg", "HS256");
                    })
                    .claims(c -> c.putAll(claimsMap))
                    .subject(publicId)
                    .issuedAt(iat)
                    .expiresAt(exp)
                    .build();

            List<GrantedAuthority> authorities = extractAuthorities(claimsMap);

            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities, publicId);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.warn("JWT filter failed: {} path={}", e.getMessage(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private static List<GrantedAuthority> extractAuthorities(Map<String, Object> claims) {
        Object rolesObj = claims.get("roles");
        List<GrantedAuthority> out = new ArrayList<>();

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

    private static String trimToNull(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
}

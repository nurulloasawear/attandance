package com.attendance.attendanceservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        try {
            String header = req.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(req, res);
                return;
            }

            String token = header.substring(7).trim();
            if (token.isBlank() || !jwtService.isValid(token)) {
                chain.doFilter(req, res);
                return;
            }

            String username = jwtService.extractSubject(token);
            String publicId = jwtService.extractClaimString(token, "publicId");

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            List<String> roles = jwtService.extractClaimStringList(token, "roles"); // <-- добавим метод ниже
            if (roles != null) {
                for (String r : roles) {
                    String role = normalizeRole(r);
                    if (role != null) authorities.add(new SimpleGrantedAuthority(role));
                }
            } else {
                String role = normalizeRole(jwtService.extractClaimString(token, "role"));
                if (role != null) authorities.add(new SimpleGrantedAuthority(role));
            }

            var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            auth.setDetails(publicId);

            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(req, res);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            chain.doFilter(req, res);
        }
    }

    private String normalizeRole(String role) {
        if (role == null) return null;
        role = role.trim();
        if (role.isEmpty()) return null;
        if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
        return role;
    }
}

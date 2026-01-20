package com.attendance.attendanceservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) {
        try {
            String header = req.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(req, res);
                return;
            }

            String token = header.substring(7);
            if (!jwtService.isValid(token)) {
                chain.doFilter(req, res);
                return;
            }

            String username = jwtService.extractSubject(token);
            String role = jwtService.extractClaimString(token, "role");
            String publicId = jwtService.extractClaimString(token, "publicId");

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            auth.setDetails(publicId);

            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(req, res);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            try {
                chain.doFilter(req, res);
            } catch (Exception ignored) {}
        }
    }
}

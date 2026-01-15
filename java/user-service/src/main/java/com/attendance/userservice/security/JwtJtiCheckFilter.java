package com.attendance.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtJtiCheckFilter extends OncePerRequestFilter {

    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String jti = jwtAuth.getToken().getClaimAsString("jti");
            if (jti != null && !redisTokenService.isAccessTokenAlive(jti)) {
                response.setStatus(401);
                response.getWriter().write("Access token revoked");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

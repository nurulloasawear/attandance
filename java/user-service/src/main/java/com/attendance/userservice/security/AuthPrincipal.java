package com.attendance.userservice.security;

public record AuthPrincipal(
        String username,
        String uid,
        String role,
        String sid,
        String jti
) {}

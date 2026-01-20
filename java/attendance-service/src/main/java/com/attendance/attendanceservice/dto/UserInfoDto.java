package com.attendance.attendanceservice.dto;

import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String publicId,
        String username,
        String role,
        boolean active
) {}

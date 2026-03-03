package com.groupservice.groupservice.dto;

public record GroupUpdateRequest(
        String name,
        String department,
        String leaderUserPublicId
) {}
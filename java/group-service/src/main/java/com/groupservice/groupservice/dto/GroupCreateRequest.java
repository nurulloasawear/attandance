package com.groupservice.groupservice.dto;

import jakarta.validation.constraints.NotBlank;

public record GroupCreateRequest(
        @NotBlank String name,
        String department,
        String leaderUserPublicId
) {}
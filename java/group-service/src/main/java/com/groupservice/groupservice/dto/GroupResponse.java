package com.groupservice.groupservice.dto;

import java.util.List;

public record GroupResponse(
        String groupPublicId,
        String name,
        String department,
        String leaderUserPublicId,
        List<MemberResponse> members
) {}
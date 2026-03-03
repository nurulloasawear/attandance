package com.groupservice.groupservice.mapper;

import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.dto.MemberResponse;
import com.groupservice.groupservice.model.Group;
import com.groupservice.groupservice.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupMapper {

    private final GroupMemberRepository memberRepo;

    public GroupResponse toResponse(Group g) {
        var members = memberRepo.findAllByGroup_GroupPublicIdOrderByJoinedAtAsc(g.getGroupPublicId())
                .stream()
                .map(m -> new MemberResponse(m.getUserPublicId()))
                .toList();

        return new GroupResponse(
                g.getGroupPublicId(),
                g.getName(),
                g.getDepartment(),
                g.getLeaderUserPublicId(),
                members
        );
    }
}
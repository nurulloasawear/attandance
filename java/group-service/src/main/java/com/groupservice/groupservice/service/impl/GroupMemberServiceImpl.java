package com.groupservice.groupservice.service.impl;

import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.model.Group;
import com.groupservice.groupservice.model.GroupMember;
import com.groupservice.groupservice.repository.GroupMemberRepository;
import com.groupservice.groupservice.repository.GroupRepository;
import com.groupservice.groupservice.service.GroupMemberService;
import com.groupservice.groupservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final GroupService groupService;

    @Override
    @Transactional
    public GroupResponse addMember(String groupPublicId, String userPublicId) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));

        GroupMember m = GroupMember.builder()
                .group(g)
                .userPublicId(userPublicId.trim())
                .joinedAt(Instant.now())
                .build();

        try {
            memberRepo.save(m);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("User already belongs to another group");
        }

        return groupService.get(groupPublicId);
    }

    @Override
    @Transactional
    public GroupResponse removeMember(String groupPublicId, String userPublicId) {
        memberRepo.deleteByGroup_GroupPublicIdAndUserPublicId(groupPublicId, userPublicId.trim());
        return groupService.get(groupPublicId);
    }

    @Override
    public GroupResponse myGroup(String myUserPublicId) {
        var mem = memberRepo.findByUserPublicId(myUserPublicId.trim())
                .orElseThrow(() -> new IllegalArgumentException("You are not in any group"));
        return groupService.get(mem.getGroup().getGroupPublicId());
    }
}
package com.groupservice.groupservice.service.impl;

import com.groupservice.groupservice.dto.GroupCreateRequest;
import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.dto.GroupUpdateRequest;
import com.groupservice.groupservice.dto.MemberResponse;
import com.groupservice.groupservice.model.Group;
import com.groupservice.groupservice.repository.GroupMemberRepository;
import com.groupservice.groupservice.repository.GroupRepository;
import com.groupservice.groupservice.service.GroupService;
import com.groupservice.groupservice.service.PublicGroupIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final PublicGroupIdGenerator idGen;

    @Override
    @Transactional
    public GroupResponse create(GroupCreateRequest req) {
        String gid = idGen.nextGroupPublicId();

        Group g = Group.builder()
                .groupPublicId(gid)
                .name(req.name().trim())
                .department(req.department() == null ? null : req.department().trim())
                .leaderUserPublicId(req.leaderUserPublicId() == null ? null : req.leaderUserPublicId().trim())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        try {
            groupRepo.save(g);
        } catch (DataIntegrityViolationException e) {
            // ⚠️ если leader уже занят — упадёт на unique index
            throw new IllegalArgumentException("Leader already assigned to another group");
        }

        return toResponse(g);
    }

    @Override
    public GroupResponse get(String groupPublicId) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));
        return toResponse(g);
    }

    @Override
    public List<GroupResponse> list() {
        return groupRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public GroupResponse update(String groupPublicId, GroupUpdateRequest req) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));

        if (req.name() != null) g.setName(req.name().trim());
        if (req.department() != null) g.setDepartment(req.department().trim());
        if (req.leaderUserPublicId() != null) g.setLeaderUserPublicId(req.leaderUserPublicId().trim());

        try {
            groupRepo.save(g);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Leader already assigned to another group");
        }

        return toResponse(g);
    }

    @Override
    @Transactional
    public void delete(String groupPublicId) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));
        groupRepo.delete(g);
    }

    private GroupResponse toResponse(Group g) {
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
package com.groupservice.groupservice.service.impl;

import com.groupservice.groupservice.dto.GroupCreateRequest;
import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.dto.GroupUpdateRequest;
import com.groupservice.groupservice.mapper.GroupMapper;
import com.groupservice.groupservice.model.Group;
import com.groupservice.groupservice.repository.GroupRepository;
import com.groupservice.groupservice.service.GroupIdGenerator;
import com.groupservice.groupservice.service.GroupService;
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
    private final GroupIdGenerator idGen;
    private final GroupMapper mapper;

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
            throw new IllegalArgumentException("Leader already assigned to another group");
        }

        return mapper.toResponse(g);
    }

    @Override
    public GroupResponse get(String groupPublicId) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));
        return mapper.toResponse(g);
    }

    @Override
    public List<GroupResponse> list() {
        return groupRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public GroupResponse update(String groupPublicId, GroupUpdateRequest req) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));

        if (req.name() != null) g.setName(req.name().trim());
        if (req.department() != null) g.setDepartment(req.department().trim());
        if (req.leaderUserPublicId() != null) g.setLeaderUserPublicId(req.leaderUserPublicId().trim());
        g.setUpdatedAt(Instant.now());

        try {
            groupRepo.save(g);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Leader already assigned to another group");
        }

        return mapper.toResponse(g);
    }

    @Override
    @Transactional
    public void delete(String groupPublicId) {
        Group g = groupRepo.findByGroupPublicId(groupPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupPublicId));
        groupRepo.delete(g);
    }
}
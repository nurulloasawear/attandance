package com.groupservice.groupservice.controller;

import com.groupservice.groupservice.dto.GroupCreateRequest;
import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.dto.GroupUpdateRequest;
import com.groupservice.groupservice.service.GroupMemberService;
import com.groupservice.groupservice.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/groups", produces = "application/json")
@RequiredArgsConstructor
public class AdminGroupController {

    private final GroupService groupService;
    private final GroupMemberService memberService;

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupCreateRequest req) {
        return ResponseEntity.status(201).body(groupService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> list() {
        return ResponseEntity.ok(groupService.list());
    }

    @GetMapping("/{groupPublicId}")
    public ResponseEntity<GroupResponse> get(@PathVariable String groupPublicId) {
        return ResponseEntity.ok(groupService.get(groupPublicId));
    }

    @PatchMapping("/{groupPublicId}")
    public ResponseEntity<GroupResponse> update(
            @PathVariable String groupPublicId,
            @RequestBody GroupUpdateRequest req
    ) {
        return ResponseEntity.ok(groupService.update(groupPublicId, req));
    }

    @DeleteMapping("/{groupPublicId}")
    public ResponseEntity<?> delete(@PathVariable String groupPublicId) {
        groupService.delete(groupPublicId);
        return ResponseEntity.ok().body(java.util.Map.of("status", "deleted", "groupPublicId", groupPublicId));
    }

    @PostMapping("/{groupPublicId}/members/{userPublicId}")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable String groupPublicId,
            @PathVariable String userPublicId
    ) {
        return ResponseEntity.ok(memberService.addMember(groupPublicId, userPublicId));
    }

    @DeleteMapping("/{groupPublicId}/members/{userPublicId}")
    public ResponseEntity<GroupResponse> removeMember(
            @PathVariable String groupPublicId,
            @PathVariable String userPublicId
    ) {
        return ResponseEntity.ok(memberService.removeMember(groupPublicId, userPublicId));
    }
}
package com.groupservice.groupservice.controller;

import com.groupservice.groupservice.dto.GroupResponse;
import com.groupservice.groupservice.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/internal/groups", produces = "application/json")
@RequiredArgsConstructor
public class InternalGroupController {

    private final GroupMemberService memberService;

    @GetMapping("/my")
    public ResponseEntity<GroupResponse> myGroup(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        String myPublicId = jwt.getClaimAsString("publicId"); // или uid, как у тебя
        return ResponseEntity.ok(memberService.myGroup(myPublicId));
    }
}
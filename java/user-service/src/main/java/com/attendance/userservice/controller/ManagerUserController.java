package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.dto.ActorContext;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.service.manager.ManagerUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_MANAGER')")
public class ManagerUserController {

    private final ManagerUserService managerUserService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(managerUserService.getAllUsers());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<UserDto> getEmployee(@PathVariable String publicId) {
        requirePublicId(publicId);
        return ResponseEntity.ok(managerUserService.getEmployeeByPublicId(publicId));
    }

    @PatchMapping("/{publicId}")
    public ResponseEntity<UserDto> updateEmployee(
            @PathVariable String publicId,
            @RequestBody UserDto dto,
            @RequestParam(required = false) String password,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        requirePublicId(publicId);

        ActorContext ctx = ActorContext.from(jwt, request);

        return ResponseEntity.ok(
                managerUserService.updateEmployeeByPublicId(
                        publicId,
                        dto,
                        password,
                        ctx
                )
        );
    }

    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateEmployee(
            @PathVariable String publicId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        requirePublicId(publicId);

        ActorContext ctx = ActorContext.from(jwt, request);

        managerUserService.deactivateEmployeeByPublicId(publicId, ctx);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Employee deactivated",
                "publicId", publicId
        ));
    }

    private void requirePublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }
    }
}

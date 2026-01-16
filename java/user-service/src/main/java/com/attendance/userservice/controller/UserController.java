package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto dto,
                          @RequestParam String password) {
        return userService.createUser(dto, password);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/by-username/{username}")
    public UserDto getByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Missing or invalid access token"));
        }

        if (auth instanceof JwtAuthenticationToken jat) {
            Jwt jwt = jat.getToken();
            return ResponseEntity.ok(Map.of(
                    "username", jwt.getSubject(),
                    "uid", jwt.getClaimAsString("uid"),
                    "role", jwt.getClaimAsString("role"),
                    "sid", jwt.getClaimAsString("sid"),
                    "jti", jwt.getClaimAsString("jti")
            ));
        }

        return ResponseEntity.ok(Map.of(
                "principal", auth.getPrincipal().toString(),
                "authorities", auth.getAuthorities().toString()
        ));
    }

    @GetMapping("/{id}/role")
    public String getRoleById(@PathVariable UUID id) {
        return userService.getUserRoleById(id);
    }

    @GetMapping("/by-username/{username}/role")
    public String getRoleByUsername(@PathVariable String username) {
        return userService.getUserRoleByUsername(username);
    }

    @GetMapping("/roles")
    public String[] getAllRoles() {
        return Arrays.stream(RoleType.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        userService.deleteUserById(id);
    }

    @DeleteMapping("/by-username/{username}")
    public void deleteByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
    }

    @DeleteMapping("/by-email/{email}")
    public void deleteByEmail(@PathVariable String email) {
        userService.deleteUserByEmail(email);
    }
}

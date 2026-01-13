package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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

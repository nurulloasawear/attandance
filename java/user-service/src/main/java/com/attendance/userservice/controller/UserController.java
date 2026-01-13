package com.attendance.userservice.controller;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto dto, @RequestParam String password) {
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
}

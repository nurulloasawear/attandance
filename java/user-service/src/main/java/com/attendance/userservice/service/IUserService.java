package com.attendance.userservice.service;

import com.attendance.commonlib.dto.UserDto;
import java.util.UUID;

public interface IUserService {
    UserDto createUser(UserDto userDto, String password);
    UserDto getUserById(UUID id);
    String getUserRoleById(UUID id);
    UserDto getUserByUsername(String username);
    void deleteUserById(UUID id);

    void deleteUserByUsername(String username);

    void deleteUserByEmail(String email);
    String getUserRoleByUsername(String username);
}
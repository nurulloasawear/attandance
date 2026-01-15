package com.attendance.userservice.service;

import com.attendance.commonlib.dto.UserDto;
import java.util.UUID;

public interface IUserService {
    UserDto getUserById(UUID id);
    UserDto getUserByUsername(String username);
    UserDto createUser(UserDto userDto, String rawPassword);
    String getUserRoleById(UUID id);
    String getUserRoleByUsername(String username);

    void deleteUserById(UUID id);
    void deleteUserByUsername(String username);
    void deleteUserByEmail(String email);
}

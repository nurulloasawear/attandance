package com.attendance.userservice.service;

import com.attendance.commonlib.dto.UserDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface IUserService {


    UserDto getUserById(UUID id);
    UserDto getUserByUsername(String username);
    UserDto createUser(UserDto userDto, String rawPassword);

    UserDto getUserByPublicId(String publicId);

    String getUserRoleById(UUID id);
    String getUserRoleByUsername(String username);

    String getUserRoleByPublicId(String publicId);

    void deleteUserById(UUID id);
    void deleteUserByUsername(String username);
    void deleteUserByEmail(String email);

    @Transactional
    void deleteUserByPublicId(String publicId);

    @Transactional
    void deactivateUserByPublicId(String publicId, String actorPublicId, String sessionId, String ip, String device);
}

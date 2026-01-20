package com.attendance.userservice.service;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.dto.DeviceDto;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    UserDto createUser(UserDto userDto, String rawPassword);
    List<DeviceDto> getAllDevices();
    void banDevice(UUID deviceId, String reason);
    void unbanDevice(UUID deviceId);


    UserDto getUserById(UUID id);

    UserDto getUserByUsername(String username);

    UserDto getUserByPublicId(String publicId);

    List<UserDto> getAllUsers();
    String getUserRoleById(UUID id);

    String getUserRoleByUsername(String username);

    String getUserRoleByPublicId(String publicId);
    UserDto getMyProfile(String myPublicId);

    UserDto updateMyProfile(
            String myPublicId,
            UserDto dto,
            String rawPassword,
            String sessionId,
            String ip,
            String device
    );

    void deleteMyAccount(
            String myPublicId,
            String sessionId,
            String ip,
            String device
    );

    void changeRoleBySuperAdmin(
            String targetPublicId,
            String newRole,
            String actorPublicId,
            String sessionId,
            String ip,
            String device
    );

    UserDto updateUserByPublicId(
            String publicId,
            UserDto dto,
            String rawPassword,
            String actorPublicId,
            String sessionId,
            String ip,
            String device
    );

    void deactivateUserByPublicId(
            String publicId,
            String actorPublicId,
            String sessionId,
            String ip,
            String device
    );


    void deleteUserById(UUID id);

    void deleteUserByUsername(String username);

    void deleteUserByEmail(String email);

    void deleteUserByPublicId(String publicId);
}

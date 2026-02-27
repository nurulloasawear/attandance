package com.attendance.userservice.service;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.dto.AdminDeviceDto;
import com.attendance.userservice.dto.DeviceDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IUserService {

    // ===== Public CRUD / queries =====
    UserDto createUser(UserDto userDto, String rawPassword);

    UserDto getUserById(UUID id);

    UserDto getUserByUsername(String username);

    UserDto getUserByPublicId(String publicId);

    List<UserDto> getAllUsers();

    String getUserRoleById(UUID id);

    String getUserRoleByUsername(String username);

    String getUserRoleByPublicId(String publicId);

    UserDto getMyProfile(String myPublicId);

    Map<String, Object> me(Authentication auth);

    UserDto myProfile(Authentication auth);

    UserDto updateMyProfile(
            Authentication auth,
            UserDto dto,
            String rawPassword,
            String ip,
            String device
    );

    void deleteMyAccount(
            Authentication auth,
            String ip,
            String device
    );

    // ===== Admin / SuperAdmin actions =====
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

    // ===== Devices =====
    List<DeviceDto> getAllDevices();

    void banDevice(UUID deviceId, String reason);

    void unbanDevice(UUID deviceId);

    List<AdminDeviceDto> getAllDevicesAdmin();
}
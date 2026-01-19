package com.attendance.userservice.service;

import com.attendance.commonlib.dto.UserDto;

import java.util.UUID;

public interface IUserService {

    UserDto createUser(UserDto userDto, String rawPassword);


    UserDto getUserById(UUID id);

    UserDto getUserByUsername(String username);

    UserDto getUserByPublicId(String publicId);


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

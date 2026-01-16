package com.attendance.userservice.service.impl;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.audit.UserAction;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.service.IUserService;
import com.attendance.userservice.service.UserAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicIdGeneratorImpl publicIdGenerator;

    // ✅ audit
    private final UserAuditLogService auditLogService;

    @Override
    @Transactional
    public UserDto createUser(UserDto dto, String rawPassword) {
        if (dto == null) throw Errors.badRequest("body is required");
        if (dto.getUsername() == null || dto.getUsername().isBlank()) throw Errors.badRequest("username is required");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw Errors.badRequest("email is required");
        if (rawPassword == null || rawPassword.isBlank()) throw Errors.badRequest("password is required");

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw Errors.conflict("Username already exists", Map.of("username", dto.getUsername()));
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw Errors.conflict("Email already exists", Map.of("email", dto.getEmail()));
        }

        String publicId = publicIdGenerator.generateUnique();

        User user = User.builder()
                .publicId(publicId)
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(dto.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);

        auditLogService.log(saved, UserAction.USER_CREATED,
                null, null, null, null,
                "Created via UserServiceImpl.createUser");

        return mapToDto(saved);
    }

    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("id", id.toString())));
    }

    @Override
    public UserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDto)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));
    }

    @Override
    public String getUserRoleById(UUID id) {
        return userRepository.findById(id)
                .map(User::getRole)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("id", id.toString())));
    }

    @Override
    public String getUserRoleByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getRole)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));
    }

    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("id", id.toString())));

        userRepository.delete(user); // ✅ soft delete сработает если стоит @SQLDelete

        auditLogService.log(user, UserAction.USER_DELETED,
                null, null, null, null,
                "Deleted via deleteUserById");
    }

    @Override
    @Transactional
    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));

        userRepository.delete(user);

        auditLogService.log(user, UserAction.USER_DELETED,
                null, null, null, null,
                "Deleted via deleteUserByUsername");
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("email", email)));

        userRepository.delete(user);

        auditLogService.log(user, UserAction.USER_DELETED,
                null, null, null, null,
                "Deleted via deleteUserByEmail");
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getPublicId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive()
        );
    }
}

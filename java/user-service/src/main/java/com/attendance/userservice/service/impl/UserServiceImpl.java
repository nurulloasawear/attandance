package com.attendance.userservice.service.impl;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.audit.UserAction;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.service.UserAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements com.attendance.userservice.service.IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicIdGeneratorImpl publicIdGenerator;

    private final JdbcTemplate jdbc;
    private final UserAuditLogService audit;

    @Override
    @Transactional
    public UserDto createUser(UserDto dto, String rawPassword) {
        if (dto == null) throw Errors.badRequest("body is required");
        if (dto.getUsername() == null || dto.getUsername().isBlank()) throw Errors.badRequest("username is required");
        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw Errors.badRequest("email is required");
        if (rawPassword == null || rawPassword.isBlank()) throw Errors.badRequest("password is required");

        if (userRepository.existsByUsernameAndDeletedAtIsNull(dto.getUsername())) {
            throw Errors.conflict("Username already exists", Map.of("username", dto.getUsername()));
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
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

        audit.log(
                saved,
                UserAction.USER_CREATED,
                saved.getPublicId(),
                null,
                null,
                null,
                "User created"
        );

        return mapToDto(saved);
    }


    @Override
    public UserDto getUserById(UUID id) {
        if (id == null) throw Errors.badRequest("id is required");

        return userRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::mapToDto)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("id", id.toString())));
    }

    @Override
    public UserDto getUserByUsername(String username) {
        if (username == null || username.isBlank()) throw Errors.badRequest("username is required");

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(this::mapToDto)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));
    }

    @Override
    public UserDto getUserByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) throw Errors.badRequest("publicId is required");

        return userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .map(this::mapToDto)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", publicId)));
    }



    @Override
    public String getUserRoleById(UUID id) {
        if (id == null) throw Errors.badRequest("id is required");

        return userRepository.findByIdAndDeletedAtIsNull(id)
                .map(User::getRole)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("id", id.toString())));
    }

    @Override
    public String getUserRoleByUsername(String username) {
        if (username == null || username.isBlank()) throw Errors.badRequest("username is required");

        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(User::getRole)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));
    }

    @Override
    public String getUserRoleByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) throw Errors.badRequest("publicId is required");

        return userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .map(User::getRole)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", publicId)));
    }



    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        if (id == null) throw Errors.badRequest("id is required");

        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("id", id.toString())));

        softDeleteById(user.getId());

        audit.log(
                user,
                UserAction.USER_DELETED,
                null,
                null,
                null,
                null,
                "Soft deleted user"
        );
    }

    @Override
    @Transactional
    public void deleteUserByUsername(String username) {
        if (username == null || username.isBlank()) throw Errors.badRequest("username is required");

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));

        softDeleteById(user.getId());

        audit.log(
                user,
                UserAction.USER_DELETED,
                null, null, null, null,
                "Soft deleted user"
        );
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        if (email == null || email.isBlank()) throw Errors.badRequest("email is required");

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("email", email)));

        softDeleteById(user.getId());

        audit.log(
                user,
                UserAction.USER_DELETED,
                null, null, null, null,
                "Soft deleted user"
        );
    }

    @Transactional
    @Override
    public void deleteUserByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) throw Errors.badRequest("publicId is required");

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", publicId)));

        softDeleteById(user.getId());

        audit.log(
                user,
                UserAction.USER_DELETED,
                null, null, null, null,
                "Soft deleted user by publicId"
        );
    }



    @Transactional
    @Override
    public void deactivateUserByPublicId(String publicId, String actorPublicId, String sessionId, String ip, String device) {
        if (publicId == null || publicId.isBlank()) throw Errors.badRequest("publicId is required");

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", publicId)));

        int updated = jdbc.update("""
            UPDATE users
            SET is_active = FALSE,
                updated_at = NOW(),
                updated_by = ?
            WHERE id = ?
              AND deleted_at IS NULL
              AND is_active = TRUE
        """, actorPublicId, user.getId());

        if (updated == 0) {
            throw Errors.conflict("User already deactivated or deleted", Map.of("publicId", publicId));
        }

        audit.log(
                user,
                UserAction.USER_DEACTIVATED,
                actorPublicId,
                sessionId,
                ip,
                device,
                "User deactivated"
        );
    }

    private void softDeleteById(UUID id) {
        int updated = jdbc.update("""
            UPDATE users
            SET deleted_at = NOW(),
                is_active = FALSE,
                updated_at = NOW()
            WHERE id = ?
              AND deleted_at IS NULL
        """, id);

        if (updated == 0) {
            throw Errors.conflict("User already deleted", Map.of("id", id.toString()));
        }
    }



    @Transactional
    @Override
    public UserDto updateUserByPublicId(
            String publicId,
            UserDto dto,
            String rawPassword,
            String actorPublicId,
            String sessionId,
            String ip,
            String device
    ) {
        if (publicId == null || publicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }
        if (dto == null) {
            throw Errors.badRequest("body is required");
        }

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", publicId)));

        boolean changed = false;

        if (dto.getUsername() != null) {
            String newUsername = dto.getUsername().trim();
            if (newUsername.isBlank()) throw Errors.badRequest("username cannot be blank");

            if (!newUsername.equals(user.getUsername())
                    && userRepository.existsByUsernameAndDeletedAtIsNull(newUsername)) {
                throw Errors.conflict("Username already exists", Map.of("username", newUsername));
            }

            user.setUsername(newUsername);
            changed = true;
        }

        if (dto.getEmail() != null) {
            String newEmail = dto.getEmail().trim();
            if (newEmail.isBlank()) throw Errors.badRequest("email cannot be blank");

            if (!newEmail.equals(user.getEmail())
                    && userRepository.existsByEmailAndDeletedAtIsNull(newEmail)) {
                throw Errors.conflict("Email already exists", Map.of("email", newEmail));
            }

            user.setEmail(newEmail);
            changed = true;
        }

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
            changed = true;
        }

        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
            changed = true;
        }

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
            changed = true;
        }

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;
        }

        if (!changed) {
            return mapToDto(user);
        }

        user.setUpdatedBy(actorPublicId);

        User saved = userRepository.save(user);

        audit.log(
                saved,
                UserAction.USER_UPDATED,
                actorPublicId,
                sessionId,
                ip,
                device,
                "User updated"
        );

        return mapToDto(saved);
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

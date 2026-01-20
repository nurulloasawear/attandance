package com.attendance.userservice.service.impl;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.dto.DeviceDto;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.audit.UserAction;
import com.attendance.userservice.repository.UserDeviceRepository;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.IUserService;
import com.attendance.userservice.service.UserAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PublicIdGeneratorImpl publicIdGenerator;

    private final JdbcTemplate jdbc;
    private final UserAuditLogService audit;
    private final UserDeviceRepository userDeviceRepository;

    @Override
    public UserDto getMyProfile(String myPublicId) {
        if (myPublicId == null || myPublicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }
        return getUserByPublicId(myPublicId);
    }
    @Override
    @Transactional
    public void changeRoleBySuperAdmin(
            String targetPublicId,
            String newRole,
            String actorPublicId,
            String sessionId,
            String ip,
            String device
    ) {
        require(targetPublicId, "publicId is required");
        require(newRole, "role is required");

        String normalized = normalizeRole(newRole);

        if (RoleType.ROLE_SUPER_ADMIN.name().equals(normalized)) {
            throw Errors.forbidden("Cannot assign SUPER_ADMIN role", Map.of("role", normalized));
        }

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(targetPublicId.trim())
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", targetPublicId)));

        user.setRole(normalized);
        user.setUpdatedBy(actorPublicId);

        userRepository.save(user);

        audit.log(
                user,
                com.attendance.userservice.model.audit.UserAction.USER_UPDATED,
                actorPublicId,
                sessionId,
                ip,
                device,
                "Role changed to " + normalized + " by SUPER_ADMIN"
        );
    }

    @Override
    @Transactional
    public UserDto updateMyProfile(
            String myPublicId,
            UserDto dto,
            String rawPassword,
            String sessionId,
            String ip,
            String device
    ) {
        if (myPublicId == null || myPublicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }
        if (dto == null) {
            throw Errors.badRequest("body is required");
        }

        dto.setUsername(null);
        dto.setEmail(null);
        dto.setRole(null);

        // actor = он сам
        return updateUserByPublicId(
                myPublicId,
                dto,
                rawPassword,
                myPublicId,
                sessionId,
                ip,
                device
        );
    }

    @Override
    @Transactional
    public void deleteMyAccount(
            String myPublicId,
            String sessionId,
            String ip,
            String device
    ) {
        if (myPublicId == null || myPublicId.isBlank()) {
            throw Errors.badRequest("publicId is required");
        }

        // ✅ soft delete уже логируется внутри deleteUserByPublicId
        deleteUserByPublicId(myPublicId);

        // 💡 если хочешь отдельно логировать как "self delete" — можно так:
    /*
    User user = userRepository.findByPublicIdAndDeletedAtIsNull(myPublicId)
            .orElse(null);
    if (user != null) {
        audit.log(user, UserAction.USER_DELETED, myPublicId, sessionId, ip, device, "User deleted own account");
    }
    */
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<DeviceDto> getAllDevices() {
        return userDeviceRepository.findAllByOrderByLastSeenAtDesc()
                .stream()
                .map(d -> DeviceDto.builder()
                        .id(d.getId())
                        .userPublicId(d.getUser().getPublicId())
                        .sessionId(d.getSessionId())
                        .ip(d.getIp())
                        .userAgent(d.getUserAgent())
                        .firstSeenAt(d.getFirstSeenAt())
                        .lastSeenAt(d.getLastSeenAt())
                        .banned(d.isBanned())
                        .bannedAt(d.getBannedAt())
                        .bannedReason(d.getBannedReason())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void banDevice(UUID deviceId, String reason) {
        var device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> Errors.notFound("Device not found", Map.of("deviceId", deviceId.toString())));

        device.setBanned(true);
        device.setBannedAt(Instant.now());
        device.setBannedReason(reason == null ? "banned" : reason);

        userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public void unbanDevice(UUID deviceId) {
        var device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> Errors.notFound("Device not found", Map.of("deviceId", deviceId.toString())));

        device.setBanned(false);
        device.setBannedAt(null);
        device.setBannedReason(null);

        userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto dto, String rawPassword) {

        if (dto == null) throw Errors.badRequest("body is required");
        require(dto.getUsername(), "username is required");
        require(dto.getEmail(), "email is required");
        require(rawPassword, "password is required");

        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim();

        if (username.isBlank()) throw Errors.badRequest("username is required");
        if (email.isBlank()) throw Errors.badRequest("email is required");

        String role = normalizeRole(dto.getRole());

        if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            throw Errors.conflict("Username already exists", Map.of("username", username));
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw Errors.conflict("Email already exists", Map.of("email", email));
        }

        String publicId = publicIdGenerator.generateUnique();

        User user = User.builder()
                .publicId(publicId)
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(role)
                .active(true)
                .build();

        User saved = userRepository.save(user);

        audit.log(
                saved,
                UserAction.USER_CREATED,
                saved.getPublicId(), // actor (пока сам user)
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
        require(username, "username is required");

        return userRepository.findByUsernameAndDeletedAtIsNull(username.trim())
                .map(this::mapToDto)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));
    }

    @Override
    public UserDto getUserByPublicId(String publicId) {
        require(publicId, "publicId is required");

        return userRepository.findByPublicIdAndDeletedAtIsNull(publicId.trim())
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
        require(username, "username is required");

        return userRepository.findByUsernameAndDeletedAtIsNull(username.trim())
                .map(User::getRole)
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("username", username)));
    }

    @Override
    public String getUserRoleByPublicId(String publicId) {
        require(publicId, "publicId is required");

        return userRepository.findByPublicIdAndDeletedAtIsNull(publicId.trim())
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
        require(username, "username is required");

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username.trim())
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
        require(email, "email is required");

        User user = userRepository.findByEmailAndDeletedAtIsNull(email.trim())
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("email", email)));

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
    public void deleteUserByPublicId(String publicId) {
        require(publicId, "publicId is required");

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId.trim())
                .orElseThrow(() -> Errors.notFound("User not found", Map.of("publicId", publicId)));

        softDeleteById(user.getId());

        audit.log(
                user,
                UserAction.USER_DELETED,
                null, null, null, null,
                "Soft deleted user by publicId"
        );
    }



    @Override
    @Transactional
    public void deactivateUserByPublicId(String publicId, String actorPublicId, String sessionId, String ip, String device) {
        require(publicId, "publicId is required");

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId.trim())
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



    @Override
    @Transactional
    public UserDto updateUserByPublicId(
            String publicId,
            UserDto dto,
            String rawPassword,
            String actorPublicId,
            String sessionId,
            String ip,
            String device
    ) {
        require(publicId, "publicId is required");
        if (dto == null) throw Errors.badRequest("body is required");

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId.trim())
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
            user.setRole(normalizeRole(dto.getRole()));
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

    private static void require(String value, String message) {
        if (value == null || value.isBlank()) throw Errors.badRequest(message);
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return RoleType.ROLE_EMPLOYEE.name();
        }

        String r = role.trim().toUpperCase();

        boolean ok = Arrays.stream(RoleType.values())
                .map(Enum::name)
                .anyMatch(x -> x.equals(r));

        if (!ok) {
            throw Errors.validation(
                    "Invalid role",
                    Map.of(
                            "provided", r,
                            "allowed", Arrays.stream(RoleType.values()).map(Enum::name).toList()
                    )
            );
        }

        return r;
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

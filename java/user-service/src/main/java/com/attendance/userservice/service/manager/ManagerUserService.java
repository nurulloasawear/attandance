package com.attendance.userservice.service.manager;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.userservice.dto.ActorContext;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManagerUserService {

    private final IUserService userService;
    private final UserRepository userRepository; // ✅ добавили репозиторий

    public UserDto getEmployeeByPublicId(String publicId) {
        ensureTargetIsEmployee(publicId);
        return userService.getUserByPublicId(publicId);
    }

    public UserDto updateEmployeeByPublicId(
            String publicId,
            UserDto dto,
            String rawPassword,
            ActorContext ctx
    ) {
        ensureTargetIsEmployee(publicId);

        dto.setUsername(null);
        dto.setEmail(null);
        dto.setRole(null);

        return userService.updateUserByPublicId(
                publicId,
                dto,
                rawPassword,
                ctx.actorPublicId(),
                ctx.sid(),
                ctx.ip(),
                ctx.device()
        );
    }

    public void deactivateEmployeeByPublicId(String publicId, ActorContext ctx) {
        ensureTargetIsEmployee(publicId);

        userService.deactivateUserByPublicId(
                publicId,
                ctx.actorPublicId(),
                ctx.sid(),
                ctx.ip(),
                ctx.device()
        );
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAllByRoleIgnoreCaseAndDeletedAtIsNull(
                RoleType.ROLE_EMPLOYEE.name()
        );

        return users.stream()
                .map(this::toDto)
                .toList();
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getPublicId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole(),
                u.isActive()
        );
    }

    private void ensureTargetIsEmployee(String publicId) {
        String role = userService.getUserRoleByPublicId(publicId);

        if (role == null) {
            throw Errors.notFound("User not found", Map.of("publicId", publicId));
        }

        if (!RoleType.ROLE_EMPLOYEE.name().equalsIgnoreCase(role)) {
            throw Errors.forbidden(
                    "Manager can manage only EMPLOYEE accounts",
                    Map.of("publicId", publicId, "role", role)
            );
        }
    }
}

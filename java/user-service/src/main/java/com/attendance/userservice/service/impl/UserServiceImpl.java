package com.attendance.userservice.service.impl;

import com.attendance.commonlib.dto.UserDto;
import com.attendance.commonlib.exception.ResourceNotFoundException;
import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserDto createUser(UserDto dto, String rawPassword) {

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(dto.getRole())
                .active(true)
                .build();

        return mapToDto(userRepository.save(user));
    }
    @Override
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public UserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Override
    public String getUserRoleById(UUID id) {
        return userRepository.findById(id)
                .map(User::getRole)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public String getUserRoleByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getRole)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Override
    @Transactional
    public void deleteUserById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteUserByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User", "username", username);
        }
        userRepository.deleteByUsername(username);
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("User", "email", email);
        }
        userRepository.deleteByEmail(email);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive()
        );
    }
}

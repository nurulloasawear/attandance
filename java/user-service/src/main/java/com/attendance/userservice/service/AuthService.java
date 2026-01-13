package com.attendance.userservice.service;

import com.attendance.commonlib.exception.ResourceNotFoundException;
import com.attendance.userservice.dto.AuthResponse;
import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!user.isActive()) {
            throw new IllegalStateException("User is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                Map.of(
                        "userId", user.getId().toString(),
                        "role", user.getRole()
                )
        );

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}

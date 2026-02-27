package com.attendance.userservice.config;

import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.impl.PublicIdGeneratorImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final PublicIdGeneratorImpl publicIdGenerator;

    @Value("${app.superadmin.username:}")
    private String username;

    @Value("${app.superadmin.email:}")
    private String email;

    @Value("${app.superadmin.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (isBlank(username) || isBlank(email) || isBlank(password)) {
            return;
        }

        boolean exists = userRepository.existsByUsernameAndDeletedAtIsNull(username)
                || userRepository.existsByEmailAndDeletedAtIsNull(email);

        if (exists) {
            return;
        }

        String publicId = publicIdGenerator.generateUnique();

        User superAdmin = User.builder()
                .publicId(publicId)
                .username(username.trim())
                .email(email.trim())
                .password(encoder.encode(password))
                .role(RoleType.ROLE_SUPER_ADMIN.name())
                .active(true)
                .build();

        userRepository.save(superAdmin);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
package com.attendance.userservice.config;

import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.RoleType;
import com.attendance.userservice.service.impl.PublicIdGeneratorImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final PublicIdGeneratorImpl publicIdGenerator;

    @Value("${app.superadmin.username}")
    private String username;

    @Value("${app.superadmin.email}")
    private String email;

    @Value("${app.superadmin.password}")
    private String password;

    @PostConstruct
    @Transactional
    public void init() {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(username)) {
            return;
        }

        String publicId = publicIdGenerator.generateUnique();

        User superAdmin = User.builder()
                .publicId(publicId)
                .username(username)
                .email(email)
                .password(encoder.encode(password))
                .role(RoleType.ROLE_SUPER_ADMIN.name())
                .active(true)
                .build();

        userRepository.save(superAdmin);
    }
}

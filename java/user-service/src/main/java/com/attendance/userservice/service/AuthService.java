package com.attendance.userservice.service;

import com.attendance.userservice.dto.*;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.RefreshToken;
import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.RefreshTokenRepository;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.JwtService;
import com.attendance.userservice.security.RedisTokenService;
import com.attendance.userservice.service.impl.PublicIdGeneratorImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final RedisTokenService redisTokenService;
    private final PublicIdGeneratorImpl publicIdGenerator;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpMs;

    @Transactional
    public AuthTokensResponse register(RegisterRequest req, String device, String ip) {
        if (userRepository.existsByUsername(req.username())) {
            throw Errors.conflict("Username already exists", Map.of("username", req.username()));
        }
        if (userRepository.existsByEmail(req.email())) {
            throw Errors.conflict("Email already exists", Map.of("email", req.email()));
        }

        String publicId = publicIdGenerator.generateUnique();

        User user = User.builder()
                .publicId(publicId)
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .role("ROLE_EMPLOYEE")
                .active(true)
                .build();

        userRepository.save(user);

        String refreshRaw = generateRefreshRaw();
        String refreshHash = sha256(refreshRaw);

        String sid = sessionService.createSession(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                refreshHash,
                device,
                ip
        );

        String access = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of(
                        "uid", user.getId().toString(),
                        "role", user.getRole(),
                        "publicId", user.getPublicId()
                ),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        saveRefreshToken(user, refreshRaw);

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid, user.getPublicId());
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest req, String device, String ip) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> Errors.unauthorized("Invalid credentials"));

        if (!user.isActive()) {
            throw Errors.forbidden("User disabled");
        }

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw Errors.unauthorized("Invalid credentials");
        }

        String refreshRaw = generateRefreshRaw();
        String refreshHash = sha256(refreshRaw);

        String sid = sessionService.createSession(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                refreshHash,
                device,
                ip
        );

        String access = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of(
                        "uid", user.getId().toString(),
                        "role", user.getRole(),
                        "publicId", user.getPublicId()
                ),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        saveRefreshToken(user, refreshRaw);

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid, user.getPublicId());
    }

    @Transactional
    public void logoutByAccessToken(String authHeader) {
        String token = extractBearer(authHeader);
        if (token == null) return;
        if (!jwtService.isValid(token)) return;

        String jti = jwtService.extractJti(token);
        if (jti != null) {
            redisTokenService.revokeAccessToken(jti);
        }

        String sid = jwtService.extractSessionId(token);

        String uidStr = jwtService.extractClaim(token, c -> {
            Object uid = c.get("uid");
            return uid == null ? null : uid.toString();
        });

        if (sid != null && uidStr != null) {
            sessionService.revokeSession(UUID.fromString(uidStr), sid);
        }
    }

    private static String extractBearer(String header) {
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshRequest req) {
        if (req.refreshToken() == null || req.refreshToken().isBlank()) {
            throw Errors.badRequest("refreshToken is required");
        }
        if (req.sessionId() == null || req.sessionId().isBlank()) {
            throw Errors.badRequest("sessionId is required");
        }

        String oldHash = sha256(req.refreshToken());

        RefreshToken old = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> Errors.tokenInvalid("Invalid refresh token"));

        if (old.isRevoked()) {
            throw Errors.tokenInvalid("Refresh token revoked");
        }
        if (old.getExpiresAt().isBefore(Instant.now())) {
            throw Errors.tokenExpired("Refresh token expired");
        }

        User user = old.getUser();
        if (user == null) {
            throw Errors.tokenInvalid("Invalid refresh token");
        }
        if (!user.isActive()) {
            throw Errors.forbidden("User disabled");
        }

        old.setRevoked(true);
        refreshTokenRepository.save(old);

        String newRefreshRaw = generateRefreshRaw();
        saveRefreshToken(user, newRefreshRaw);

        String sid = req.sessionId();

        String access = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of(
                        "uid", user.getId().toString(),
                        "role", user.getRole(),
                        "publicId", user.getPublicId()
                ),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        return new AuthTokensResponse(access, newRefreshRaw, "Bearer", sid, user.getPublicId());
    }

    private String generateRefreshRaw() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private void saveRefreshToken(User user, String refreshRaw) {
        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(sha256(refreshRaw))
                .expiresAt(Instant.now().plusMillis(refreshExpMs))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(rt);
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

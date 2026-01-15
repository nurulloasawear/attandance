package com.attendance.userservice.service;

import com.attendance.userservice.dto.*;
import com.attendance.userservice.model.RefreshToken;
import com.attendance.userservice.model.User;
import com.attendance.userservice.repository.RefreshTokenRepository;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.JwtService;
import com.attendance.userservice.security.RedisTokenService;
import com.attendance.userservice.security.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpMs;

    @Transactional
    public AuthTokensResponse register(RegisterRequest req, String device, String ip) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already exists");
        }

        User user = User.builder()
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
                Map.of("uid", user.getId().toString(), "role", user.getRole()),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        saveRefreshToken(user, refreshRaw);

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid);
    }

    @Transactional
    public void logoutByAccessToken(String authHeader) {
        String token = extractBearer(authHeader);
        if (token == null || !jwtService.isValid(token)) return;

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
    public AuthTokensResponse login(LoginRequest req, String device, String ip) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new IllegalStateException("Invalid credentials"));

        if (!user.isActive()) throw new IllegalStateException("User disabled");
        if (!passwordEncoder.matches(req.password(), user.getPassword()))
            throw new IllegalStateException("Invalid credentials");

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
                Map.of("uid", user.getId().toString(), "role", user.getRole()),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        saveRefreshToken(user, refreshRaw);

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid);
    }


    @Transactional
    public AuthTokensResponse refresh(RefreshRequest req) {
        String oldHash = sha256(req.refreshToken());

        RefreshToken old = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));

        if (old.isRevoked() || old.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Invalid refresh token");
        }

        User user = old.getUser();
        if (user == null || !user.isActive()) {
            throw new IllegalStateException("Invalid refresh token");
        }

        // rotate refresh
        old.setRevoked(true);
        refreshTokenRepository.save(old);

        String newRefreshRaw = generateRefreshRaw();
        saveRefreshToken(user, newRefreshRaw);

        // ✅ тот же sid
        String sid = req.sessionId();

        String access = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of("uid", user.getId().toString(), "role", user.getRole()),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        return new AuthTokensResponse(access, newRefreshRaw, "Bearer", sid);
    }


    @Transactional
    public void logout(RefreshRequest req) {
        String hash = sha256(req.refreshToken());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private String generateRefreshRaw() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private void saveRefreshToken(User user, String refreshRaw) {
        RefreshToken rt = RefreshToken.builder()
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

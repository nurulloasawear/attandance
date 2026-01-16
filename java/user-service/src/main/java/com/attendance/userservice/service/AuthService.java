package com.attendance.userservice.service;

import com.attendance.userservice.dto.*;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.RefreshToken;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.audit.UserAction;
import com.attendance.userservice.repository.RefreshTokenRepository;
import com.attendance.userservice.repository.UserRepository;
import com.attendance.userservice.security.JwtService;
import com.attendance.userservice.security.RedisTokenService;
import com.attendance.userservice.service.SessionService;
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

    private final UserAuditLogService auditLogService;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpMs;

    @Transactional
    public AuthTokensResponse register(RegisterRequest req, String device, String ip) {
        if (req == null) throw Errors.badRequest("body is required");
        if (req.username() == null || req.username().isBlank()) throw Errors.badRequest("username is required");
        if (req.email() == null || req.email().isBlank()) throw Errors.badRequest("email is required");
        if (req.password() == null || req.password().isBlank()) throw Errors.badRequest("password is required");

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

        auditLogService.log(user, UserAction.USER_CREATED,
                user.getPublicId(), // actor (сам себя создал)
                sid, ip, device,
                "Registered");

        // ✅ Kafka event (если включишь)
        // eventPublisher.publish(new UserEvent(...));

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid, user.getPublicId());
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest req, String device, String ip) {
        if (req == null) throw Errors.badRequest("body is required");
        if (req.username() == null || req.username().isBlank()) throw Errors.badRequest("username is required");
        if (req.password() == null || req.password().isBlank()) throw Errors.badRequest("password is required");

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

        auditLogService.log(user, UserAction.USER_UPDATED,
                user.getPublicId(),
                sid, ip, device,
                "Login (new session created)");

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

        String publicId = jwtService.extractClaim(token, c -> {
            Object pid = c.get("publicId");
            return pid == null ? null : pid.toString();
        });

        if (sid != null && uidStr != null) {
            UUID uid = UUID.fromString(uidStr);
            sessionService.revokeSession(uid, sid);

            userRepository.findById(uid).ifPresent(user ->
                    auditLogService.log(user, UserAction.USER_UPDATED,
                            publicId != null ? publicId : user.getPublicId(),
                            sid, null, null,
                            "Logout (session revoked)")
            );
        }
    }

    @Transactional
    public AuthTokensResponse refresh(RefreshRequest req) {
        if (req == null) throw Errors.badRequest("body is required");

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

        auditLogService.log(user, UserAction.USER_UPDATED,
                user.getPublicId(),
                sid, null, null,
                "Refresh token rotated");

        return new AuthTokensResponse(access, newRefreshRaw, "Bearer", sid, user.getPublicId());
    }

    private static String extractBearer(String header) {
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7);
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

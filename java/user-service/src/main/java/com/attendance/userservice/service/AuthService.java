package com.attendance.userservice.service;

import com.attendance.userservice.dto.*;
import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.RefreshToken;
import com.attendance.userservice.model.User;
import com.attendance.userservice.model.audit.UserAction;
import com.attendance.userservice.service.UserAuditLogService;
import com.attendance.userservice.repository.RefreshTokenRepository;
import com.attendance.userservice.security.JwtService;
import com.attendance.userservice.security.RedisTokenService;
import com.attendance.userservice.service.impl.PublicIdGeneratorImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JdbcTemplate jdbc;

    private final RefreshTokenRepository refreshTokenRepository; // пока оставим как есть
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

        if (existsActiveByUsername(req.username())) {
            throw Errors.conflict("Username already exists", Map.of("username", req.username()));
        }
        if (existsActiveByEmail(req.email())) {
            throw Errors.conflict("Email already exists", Map.of("email", req.email()));
        }

        UUID userId = UUID.randomUUID();
        String publicId = publicIdGenerator.generateUnique();

        String encoded = passwordEncoder.encode(req.password());

        int inserted = jdbc.update("""
            INSERT INTO users (
                id, public_id, username, password, email,
                first_name, last_name, role, is_active,
                created_at, updated_at, deleted_at,
                created_by, updated_by
            )
            VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, 'ROLE_EMPLOYEE', TRUE,
                NOW(), NOW(), NULL,
                ?, ?
            )
        """,
                userId,
                publicId,
                req.username(),
                encoded,
                req.email(),
                req.firstName(),
                req.lastName(),
                publicId,
                publicId
        );

        if (inserted != 1) {
            throw Errors.internal("Failed to create user");
        }


        User user = new User();
        user.setId(userId);
        user.setPublicId(publicId);
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(encoded);
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setRole("ROLE_EMPLOYEE");
        user.setActive(true);

        String refreshRaw = generateRefreshRaw();
        String refreshHash = sha256(refreshRaw);

        String sid = sessionService.createSession(
                userId,
                user.getUsername(),
                user.getRole(),
                refreshHash,
                device,
                ip
        );

        String access = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of(
                        "uid", userId.toString(),
                        "role", user.getRole(),
                        "publicId", publicId
                ),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        saveRefreshToken(user, refreshRaw);

        auditLogService.log(
                user,
                UserAction.USER_CREATED,
                publicId,
                sid,
                ip,
                device,
                "Registered"
        );

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid, publicId);
    }

    @Transactional
    public AuthTokensResponse login(LoginRequest req, String device, String ip) {
        if (req == null) throw Errors.badRequest("body is required");
        if (req.username() == null || req.username().isBlank()) throw Errors.badRequest("username is required");
        if (req.password() == null || req.password().isBlank()) throw Errors.badRequest("password is required");

        User user = findActiveUserByUsername(req.username())
                .orElseThrow(() -> Errors.unauthorized("Invalid credentials"));

        if (!user.isActive()) throw Errors.forbidden("User disabled");
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

        auditLogService.log(
                user,
                UserAction.LOGIN,
                user.getPublicId(),
                sid,
                ip,
                device,
                "Login (new session created)"
        );

        return new AuthTokensResponse(access, refreshRaw, "Bearer", sid, user.getPublicId());
    }

    @Transactional
    public void logoutByAccessToken(String authHeader) {
        String token = extractBearer(authHeader);
        if (token == null) return;
        if (!jwtService.isValid(token)) return;

        String sid = jwtService.extractSessionId(token);

        String uidStr = jwtService.extractClaim(token, c -> {
            Object uid = c.get("uid");
            return uid == null ? null : uid.toString();
        });

        String publicId = jwtService.extractClaim(token, c -> {
            Object pid = c.get("publicId");
            return pid == null ? null : pid.toString();
        });

        String jti = jwtService.extractJti(token);
        if (jti != null) redisTokenService.revokeAccessToken(jti);

        if (sid == null || uidStr == null) return;

        UUID uid = UUID.fromString(uidStr);
        sessionService.revokeSession(uid, sid);


        findActiveUserById(uid).ifPresent(user ->
                auditLogService.log(
                        user,
                        UserAction.LOGOUT,
                        (publicId != null ? publicId : user.getPublicId()),
                        sid,
                        null,
                        null,
                        "Logout (session revoked)"
                )
        );
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

        if (old.isRevoked()) throw Errors.tokenInvalid("Refresh token revoked");
        if (old.getExpiresAt().isBefore(Instant.now())) throw Errors.tokenExpired("Refresh token expired");

        User user = old.getUser();
        if (user == null) throw Errors.tokenInvalid("Invalid refresh token");


        User dbUser = findActiveUserById(user.getId())
                .orElseThrow(() -> Errors.tokenInvalid("User not found (deleted)"));

        if (!dbUser.isActive()) throw Errors.forbidden("User disabled");

        old.setRevoked(true);
        refreshTokenRepository.save(old);

        String newRefreshRaw = generateRefreshRaw();
        saveRefreshToken(dbUser, newRefreshRaw);

        String sid = req.sessionId();

        String access = jwtService.generateAccessToken(
                dbUser.getUsername(),
                Map.of(
                        "uid", dbUser.getId().toString(),
                        "role", dbUser.getRole(),
                        "publicId", dbUser.getPublicId()
                ),
                sid
        );

        String jti = jwtService.extractJti(access);
        redisTokenService.storeAccessToken(jti, sid, jwtService.getAccessExpirationMs());

        auditLogService.log(
                dbUser,
                UserAction.REFRESH,
                dbUser.getPublicId(),
                sid,
                null,
                null,
                "Refresh token rotated"
        );

        return new AuthTokensResponse(access, newRefreshRaw, "Bearer", sid, dbUser.getPublicId());
    }


    private boolean existsActiveByUsername(String username) {
        Integer x = jdbc.queryForObject("""
            SELECT 1
            FROM users
            WHERE username = ?
              AND deleted_at IS NULL
            LIMIT 1
        """, Integer.class, username);
        return x != null;
    }

    private boolean existsActiveByEmail(String email) {
        Integer x = jdbc.queryForObject("""
            SELECT 1
            FROM users
            WHERE email = ?
              AND deleted_at IS NULL
            LIMIT 1
        """, Integer.class, email);
        return x != null;
    }

    private Optional<User> findActiveUserByUsername(String username) {
        return jdbc.query("""
            SELECT id, public_id, username, password, email, first_name, last_name, role, is_active
            FROM users
            WHERE username = ?
              AND deleted_at IS NULL
            LIMIT 1
        """, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapUser(rs));
        }, username);
    }

    private Optional<User> findActiveUserById(UUID id) {
        return jdbc.query("""
            SELECT id, public_id, username, password, email, first_name, last_name, role, is_active
            FROM users
            WHERE id = ?
              AND deleted_at IS NULL
            LIMIT 1
        """, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(mapUser(rs));
        }, id);
    }

    private static User mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        User u = new User();
        u.setId(UUID.fromString(rs.getString("id")));
        u.setPublicId(rs.getString("public_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        return u;
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

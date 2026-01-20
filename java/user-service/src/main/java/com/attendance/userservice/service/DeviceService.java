package com.attendance.userservice.service;

import com.attendance.userservice.error.Errors;
import com.attendance.userservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public void checkNotBanned(UUID userId, String deviceKey) {
        if (userId == null) {
            throw Errors.badRequest("userId is required");
        }

        String key = normalizeDeviceKey(deviceKey);

        var row = jdbc.query(
                """
                SELECT banned, banned_reason
                FROM user_devices
                WHERE user_id = ?
                  AND device_key = ?
                LIMIT 1
                """,
                rs -> {
                    if (!rs.next()) return null;
                    return Map.of(
                            "banned", rs.getBoolean("banned"),
                            "reason", rs.getString("banned_reason")
                    );
                },
                userId, key
        );


        if (row == null) return;

        boolean banned = Boolean.TRUE.equals(row.get("banned"));
        if (!banned) return;

        String reason = (String) row.get("reason");
        if (reason == null || reason.isBlank()) reason = "banned";

        throw Errors.forbidden("This device is banned", Map.of(
                "userId", userId.toString(),
                "deviceKey", key,
                "reason", reason
        ));
    }


    @Transactional
    public void touchDevice(
            User user,
            String sessionId,
            String ip,
            String userAgent,
            String deviceKey
    ) {
        if (user == null || user.getId() == null) {
            throw Errors.badRequest("user is required");
        }

        String key = normalizeDeviceKey(deviceKey);

        // ✅ Проверяем бан
        checkNotBanned(user.getId(), key);

        // ✅ Обновляем last_seen если устройство уже было
        int updated = jdbc.update(
                """
                UPDATE user_devices
                SET last_seen_at = NOW(),
                    session_id = ?,
                    ip = ?,
                    user_agent = ?,
                    updated_at = NOW()
                WHERE user_id = ?
                  AND device_key = ?
                """,
                sessionId,
                ip,
                userAgent,
                user.getId(),
                key
        );

        if (updated == 0) {
            jdbc.update(
                    """
                    INSERT INTO user_devices(user_id, device_key, session_id, ip, user_agent)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    user.getId(),
                    key,
                    sessionId,
                    ip,
                    userAgent
            );
        }
    }


    @Transactional
    public void banDevice(UUID deviceId, String reason) {
        if (deviceId == null) {
            throw Errors.badRequest("deviceId is required");
        }

        String r = (reason == null || reason.isBlank()) ? "banned" : reason.trim();

        int updated = jdbc.update(
                """
                UPDATE user_devices
                SET banned = TRUE,
                    banned_at = NOW(),
                    banned_reason = ?,
                    updated_at = NOW()
                WHERE id = ?
                """,
                r, deviceId
        );

        if (updated == 0) {
            throw Errors.notFound("Device not found", Map.of("deviceId", deviceId.toString()));
        }
    }


    @Transactional
    public void unbanDevice(UUID deviceId) {
        if (deviceId == null) {
            throw Errors.badRequest("deviceId is required");
        }

        int updated = jdbc.update(
                """
                UPDATE user_devices
                SET banned = FALSE,
                    banned_at = NULL,
                    banned_reason = NULL,
                    updated_at = NOW()
                WHERE id = ?
                """,
                deviceId
        );

        if (updated == 0) {
            throw Errors.notFound("Device not found", Map.of("deviceId", deviceId.toString()));
        }
    }


    @Transactional
    public void banUserDevice(UUID userId, String deviceKey, String reason) {
        if (userId == null) throw Errors.badRequest("userId is required");

        String key = normalizeDeviceKey(deviceKey);
        String r = (reason == null || reason.isBlank()) ? "banned" : reason.trim();

        int updated = jdbc.update(
                """
                UPDATE user_devices
                SET banned = TRUE,
                    banned_at = NOW(),
                    banned_reason = ?,
                    updated_at = NOW()
                WHERE user_id = ?
                  AND device_key = ?
                """,
                r, userId, key
        );

        if (updated == 0) {
            throw Errors.notFound("Device not found", Map.of(
                    "userId", userId.toString(),
                    "deviceKey", key
            ));
        }
    }

    private String normalizeDeviceKey(String deviceKey) {
        if (deviceKey == null || deviceKey.isBlank()) {
            return "unknown";
        }
        return deviceKey.trim();
    }
}

package com.attendance.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final StringRedisTemplate redis;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpMs;

    public String createSession(UUID userId,
                                String username,
                                String role,
                                String refreshHash,
                                String device,
                                String ip) {

        String sessionId = UUID.randomUUID().toString();
        String sessKey = "sess:" + sessionId;
        String userSetKey = "userSess:" + userId;

        Map<String, String> data = new HashMap<>();
        data.put("uid", userId.toString());
        data.put("username", username);
        data.put("role", role);
        data.put("refreshHash", refreshHash);
        data.put("device", device == null ? "unknown" : device);
        data.put("ip", ip == null ? "unknown" : ip);
        data.put("createdAt", Instant.now().toString());
        data.put("lastSeen", Instant.now().toString());

        redis.opsForHash().putAll(sessKey, data);
        redis.expire(sessKey, refreshExpMs, TimeUnit.MILLISECONDS);
        redis.opsForSet().add(userSetKey, sessionId);

        return sessionId;
    }

    public Optional<Map<Object, Object>> findSession(String sessionId) {
        String sessKey = "sess:" + sessionId;
        if (Boolean.FALSE.equals(redis.hasKey(sessKey))) return Optional.empty();
        return Optional.of(redis.opsForHash().entries(sessKey));
    }

    public void touch(String sessionId) {
        String sessKey = "sess:" + sessionId;
        if (Boolean.TRUE.equals(redis.hasKey(sessKey))) {
            redis.opsForHash().put(sessKey, "lastSeen", Instant.now().toString());
        }
    }
    public int revokeAllExcept(UUID userId, String keepSessionId) {
        String userSetKey = "userSess:" + userId;
        Set<String> sessions = redis.opsForSet().members(userSetKey);
        if (sessions == null) return 0;

        int count = 0;
        for (String sid : sessions) {
            if (sid.equals(keepSessionId)) continue;
            redis.delete("sess:" + sid);
            redis.opsForSet().remove(userSetKey, sid);
            count++;
        }
        return count;
    }

    public void revokeSession(UUID userId, String sessionId) {
        redis.delete("sess:" + sessionId);
        redis.opsForSet().remove("userSess:" + userId, sessionId);
    }

    public List<Map<Object, Object>> listSessions(UUID userId) {
        String userSetKey = "userSess:" + userId;
        Set<String> sessions = redis.opsForSet().members(userSetKey);
        if (sessions == null) return List.of();

        List<Map<Object, Object>> result = new ArrayList<>();
        for (String sid : sessions) {
            Map<Object, Object> data = redis.opsForHash().entries("sess:" + sid);
            if (!data.isEmpty()) {
                data.put("sessionId", sid);
                result.add(data);
            }
        }
        return result;
    }
}

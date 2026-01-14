package com.attendance.userservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redis;

    private static final String ACCESS_KEY = "access:";
    private static final String SESSION_KEY = "session:";
    private static final String ACCESS_KEY_PREFIX = "access:";
    public void storeAccessToken(String jti, String sessionId, long ttlMs) {
        redis.opsForValue().set(
                ACCESS_KEY + jti,
                sessionId,
                ttlMs,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isAccessTokenValid(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(ACCESS_KEY + jti));
    }
    private String key(String jti) {
        return ACCESS_KEY_PREFIX + jti;
    }
    public Optional<String> findSessionIdByJti(String jti) {
        String v = redis.opsForValue().get("access:" + jti);
        return Optional.ofNullable(v);
    }
    public void revokeAccessToken(String jti) {
        redis.delete(ACCESS_KEY + jti);
    }
    public boolean isAccessTokenActive(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(key(jti)));
    }
    public void revokeSession(String sessionId) {
        redis.delete(SESSION_KEY + sessionId);
    }
}

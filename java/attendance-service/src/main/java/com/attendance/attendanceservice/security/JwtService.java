package com.attendance.attendanceservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret is required");
        }

        // ✅ HS256 key must be >= 32 bytes
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret must be at least 32 bytes for HS256 (now=" + bytes.length + ")"
            );
        }

        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * ✅ Проверка токена (подпись + срок действия)
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            if (exp == null) return true; // если exp нет - считаем валидным
            return exp.toInstant().isAfter(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ✅ Subject = username
     */
    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * ✅ Получить claim как String
     */
    public String extractClaimString(String token, String name) {
        Object v = parseClaims(token).get(name);
        return (v == null) ? null : String.valueOf(v);
    }

    /**
     * ✅ Внутренний парсер
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

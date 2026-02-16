package com.attendance.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessExpMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long accessExpMs
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret is required");
        }

        String s = secret.trim();

        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(s);
        } catch (Exception ignore) {
            bytes = s.getBytes(StandardCharsets.UTF_8);
        }

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret must be at least 32 bytes for HS256 (now=" + bytes.length + ")"
            );
        }

        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessExpMs = accessExpMs;
    }

    public long getAccessExpirationMs() {
        return accessExpMs;
    }


    public String generateAccessToken(String publicId, Map<String, Object> extraClaims, String sessionId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId is required");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        if (extraClaims != null) claims.putAll(extraClaims);

        String pid = publicId.trim();

        claims.putIfAbsent("publicId", pid);
        claims.putIfAbsent("uid", pid);
        claims.putIfAbsent("pid", pid);  // optional, но удобно
        claims.put("sid", sessionId.trim());

        return Jwts.builder()
                .id(jti)
                .subject(pid) // <-- sub = publicId
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpMs)))
                .signWith(key)
                .compact();
    }

    public String extractClaimString(String token, String key) {
        Object v = parseClaims(token).get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public String extractSessionId(String token) {
        return extractClaimString(token, "sid");
    }

    /** publicId сначала из claim, если нет — берём subject */
    public String extractPublicId(String token) {
        String pid = extractClaimString(token, "publicId");
        if (pid != null && !pid.isBlank()) return pid;
        String sub = parseClaims(token).getSubject();
        return (sub == null || sub.isBlank()) ? null : sub.trim();
    }

    /** Если раньше sub был username — оставим совместимость */
    public String extractUsername(String token) {
        String username = extractClaimString(token, "username");
        if (username != null && !username.isBlank()) return username;
        String sub = parseClaims(token).getSubject();
        return (sub == null) ? null : sub.trim();
    }

    public String extractRole(String token) {
        return extractClaimString(token, "role");
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

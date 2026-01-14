package com.attendance.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = accessExpMs;
    }

    public long getAccessExpirationMs() {
        return accessExpMs;
    }

    public String generateAccessToken(String subject, Map<String, Object> extraClaims, String sessionId) {
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("sid", sessionId);

        Instant now = Instant.now();

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpMs)))
                .signWith(key)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parseClaims(token);
        return resolver.apply(claims);
    }
    public String extractUid(String token) {
        return extractClaim(token, c -> {
            Object uid = c.get("uid");
            return uid == null ? null : uid.toString();
        });
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractSessionId(String token) {
        return extractClaim(token, c -> {
            Object sid = c.get("sid");
            return sid == null ? null : sid.toString();
        });
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token) {
        try {
            Claims c = parseClaims(token);
            Date exp = c.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return (Claims) Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public String extractRole(String token) {
        return extractClaim(token, claims -> {
            Object role = claims.get("role");
            return role == null ? null : role.toString();
        });
    }

}

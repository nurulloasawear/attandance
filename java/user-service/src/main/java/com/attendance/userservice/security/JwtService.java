package com.attendance.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
import java.util.concurrent.TimeUnit;
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

        return Jwts.builder()
                .id(jti) // <-- ВАЖНО: это JWT ID
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(accessExpMs)))
                .signWith(key)
                .compact();
    }



    public String extractClaimString(String token, String key) {
        Object v = parseClaims(token).get(key);
        return v == null ? null : v.toString();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }
    public String extractUid(String token) {
        return extractClaim(token, c -> {
            Object uid = c.get("uid");
            return uid == null ? null : uid.toString();
        });
    }

//    public void extend(String sessionId) {
//        redis.expire("sess:" + sessionId, refreshExpMs, TimeUnit.MILLISECONDS);
//    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
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
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
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
        return extractClaimString(token, "role");
    }

}

package com.attendance.attendanceservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
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
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            if (exp == null) return true;
            return exp.toInstant().isAfter(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractClaimString(String token, String name) {
        Object v = parseClaims(token).get(name);
        if (v == null) return null;

        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * roles может быть:
     * 1) ["ADMIN","USER"]
     * 2) "ADMIN,USER"
     * 3) "ADMIN"
     */
    public List<String> extractClaimStringList(String token, String name) {
        Object v = parseClaims(token).get(name);
        if (v == null) return null;

        // roles: ["ADMIN","USER"]
        if (v instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o == null) continue;
                String s = String.valueOf(o).trim();
                if (!s.isEmpty()) out.add(s);
            }
            return out.isEmpty() ? null : out;
        }

        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return null;

        String[] parts = s.split(",");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String x = p.trim();
            if (!x.isEmpty()) out.add(x);
        }
        return out.isEmpty() ? null : out;
    }

    public Claims extractAllClaims(String token) {
        return parseClaims(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> extractor) {
        Claims claims = parseClaims(token);
        return extractor.apply(claims);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

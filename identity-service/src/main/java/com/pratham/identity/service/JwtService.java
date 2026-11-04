package com.pratham.identity.service;

import com.pratham.identity.config.JwtKeyProvider;
import com.pratham.identity.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
    private final JwtKeyProvider keyProvider;

    public JwtService(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_LIFETIME)))
                .signWith(keyProvider.getKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(keyProvider.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
